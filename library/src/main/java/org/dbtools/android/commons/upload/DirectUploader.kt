@file:Suppress("MemberVisibilityCanBePrivate")

package org.dbtools.android.commons.upload

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.util.concurrent.atomic.AtomicBoolean


@Suppress("unused")
class DirectUploader {
    val inProgress = AtomicBoolean(false) // replace with https://github.com/Kotlin/kotlinx-atomicfu
    private var cancelRequested = false

    private val _progressStateFlow = MutableStateFlow<DirectUploadProgress>(DirectUploadProgress.Enqueued)
    val progressStateFlow: StateFlow<DirectUploadProgress> = _progressStateFlow

    private fun reset() {
        cancelRequested = false
    }

    suspend fun upload(directUploadRequest: DirectUploadRequest, dispatcher: CoroutineDispatcher = Dispatchers.IO): DirectUploadResult = withContext(dispatcher) {
        val fileSystem = directUploadRequest.fileSystem
        if (!inProgress.compareAndSet(false, true)) {
            return@withContext DirectUploadResult(false, "Upload already in progress")
        }

        try {
            reset()

            val httpClient = OkHttpClient.Builder()
                .addNetworkInterceptor(Interceptor { chain: Interceptor.Chain ->
                    val originalResponse = chain.proceed(chain.request())

                    originalResponse.newBuilder()
                        .body(ProgressResponseBody(checkNotNull(originalResponse.body)))
                        .build()
                })
                .build()

            // execute upload
            val directUploadResult = executeUpload(httpClient, directUploadRequest)

            // verify result
            if (directUploadResult.success && !fileSystem.exists(directUploadRequest.sourceFile)) {
                val message = "Upload was successful, but the target file does not exist (${directUploadRequest.sourceFile})"
                _progressStateFlow.value = DirectUploadProgress.UploadComplete(false, message)
                return@withContext DirectUploadResult(false, message)
            }

            return@withContext directUploadResult
        } catch (expected: Exception) {
            Logger.e(expected) { "Failed to Upload: DirectUploadRequest" }

            // notify observers
            _progressStateFlow.value = DirectUploadProgress.UploadComplete(false, expected.message)

            DirectUploadResult(false, expected.message)
        } finally {
            inProgress.set(false)
        }
    }

    private fun executeUpload(httpClient: OkHttpClient, directUploadRequest: DirectUploadRequest): DirectUploadResult {
        val fileSystem = directUploadRequest.fileSystem

        // create requestBody
        val multipartBodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                name = directUploadRequest.multiPartFormPartName,
                filename = directUploadRequest.sourceFile.name,
                body = fileSystem.source(directUploadRequest.sourceFile).buffer().readByteArray().toRequestBody(directUploadRequest.contentType)
            )

        directUploadRequest.formParts?.forEach { part ->
            multipartBodyBuilder.addFormDataPart(part.name, part.value)
        }

        val requestBody: RequestBody = multipartBodyBuilder.build()

        // create request
        val requestBuilder = Request.Builder()
            .url(directUploadRequest.uploadUrl)
            .post(requestBody)

        // add any custom headers
        directUploadRequest.customHeaders?.forEach { header ->
            requestBuilder.addHeader(header.name, header.value)
        }

        // build the request
        val okhttpRequest = requestBuilder.build()

        // upload NOW
        val startMs = System.currentTimeMillis()

        Logger.d { "Direct Uploading [${directUploadRequest.uploadUrl}] -> [${directUploadRequest.sourceFile}]" }
        val directUploadResult: DirectUploadResult = httpClient.newCall(okhttpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                return@use DirectUploadResult(false, "Failed to upload code: ${response.code}", response.code)
            }

            return@use if (directUploadRequest.getResponseBodyAsString) {
                DirectUploadResult(true, responseBodyString = response.body?.string())
            } else {
                DirectUploadResult(true)
            }
        }

        val totalUploadMs = System.currentTimeMillis() - startMs
        Logger.d { "Direct Upload Finished ${totalUploadMs}ms [${directUploadResult.code}] [${directUploadRequest.uploadUrl}] -> [${directUploadRequest.sourceFile}]" }

        // notify observers
        _progressStateFlow.value = DirectUploadProgress.UploadComplete(directUploadResult.success, directUploadResult.message)

        return directUploadResult
    }

    fun cancel() {
        cancelRequested = true
    }

    private inner class ProgressResponseBody(
        private val responseBody: ResponseBody
    ) : ResponseBody() {
        private val bufferedSource: BufferedSource by lazy { source(responseBody.source()).buffer() }

        override fun contentType(): MediaType? = responseBody.contentType()

        override fun contentLength(): Long = responseBody.contentLength()

        override fun source(): BufferedSource = bufferedSource

        private fun source(source: Source): Source {
            return object : ForwardingSource(source) {
                var totalBytesRead = 0L
                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead = super.read(sink, byteCount)
                    val uploadComplete = bytesRead == -1L // read() returns the number of bytes read, or -1 if this source is exhausted.

                    // update totalBytesRead
                    totalBytesRead += if (!uploadComplete) bytesRead else 0

                    // report progress
                    val progress = DirectUploadProgress.Uploading(totalBytesRead, responseBody.contentLength())
                    _progressStateFlow.value = progress

                    return bytesRead
                }
            }
        }
    }

    companion object {
        const val MAX_PROGRESS = 100
    }
}