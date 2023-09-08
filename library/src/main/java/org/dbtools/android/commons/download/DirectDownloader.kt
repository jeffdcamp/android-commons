@file:Suppress("MemberVisibilityCanBePrivate")

package org.dbtools.android.commons.download

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused")
class DirectDownloader {
    val inProgress = AtomicBoolean(false) // replace with https://github.com/Kotlin/kotlinx-atomicfu
    private var cancelRequested = false

    private val _progressStateFlow = MutableStateFlow<DirectDownloadProgress>(DirectDownloadProgress.Enqueued)
    val progressStateFlow: StateFlow<DirectDownloadProgress> = _progressStateFlow

    private fun reset() {
        cancelRequested = false
    }

    suspend fun download(directDownloadRequest: DirectDownloadRequest, dispatcher: CoroutineDispatcher = Dispatchers.IO): DirectDownloadResult = withContext(dispatcher) {
        val filesystem = directDownloadRequest.fileSystem
        if (!inProgress.compareAndSet(false, true)) {
            return@withContext DirectDownloadResult(false, "Download already in progress")
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

            // make sure target file doesn't already exist
            val prepareTargetFileResult = prepareTargetFile(directDownloadRequest)
            if (prepareTargetFileResult != null) {
                _progressStateFlow.value = DirectDownloadProgress.DownloadComplete(false, prepareTargetFileResult.message)
                return@withContext prepareTargetFileResult
            }

            // execute download
            val directDownloadResult = executeDownload(httpClient, directDownloadRequest)

            // verify result
            if (directDownloadResult.success && !filesystem.exists(directDownloadRequest.targetFile)) {
                val message = "Download was successful, but the target file does not exist (${directDownloadRequest.targetFile})"
                _progressStateFlow.value = DirectDownloadProgress.DownloadComplete(false, message)
                return@withContext DirectDownloadResult(false, message)
            }

            return@withContext directDownloadResult
        } catch (expected: Exception) {
            Logger.e(expected) { "Failed to download: directDownloadRequest" }

            // notify observers
            _progressStateFlow.value = DirectDownloadProgress.DownloadComplete(false, expected.message)

            DirectDownloadResult(false, expected.message)
        } finally {
            inProgress.set(false)
        }
    }

    /*
     * Make sure target directory exists, and target file does NOT yet exist
     */
    @Suppress("ReturnCount") // all return points are valid and needed
    private fun prepareTargetFile(directDownloadRequest: DirectDownloadRequest): DirectDownloadResult? {
        val fileSystem = directDownloadRequest.fileSystem
        val targetFile = directDownloadRequest.targetFile

        // make sure target directory exists
        try {
            val targetDirectory = targetFile.parent ?: return DirectDownloadResult(false, "Failed to prepareTargetFile target directory == null")

            if (!fileSystem.exists(targetDirectory)) {
                fileSystem.createDirectories(targetDirectory)
                if (!fileSystem.exists(targetDirectory)) {
                    return DirectDownloadResult(false, "Failed to create target directory: [${targetDirectory}]")
                }
            }
        } catch (expected: Exception) {
            val message = "Failed to create target directory: [${targetFile.parent}]  message: [${expected.message}]"
            Logger.e(expected) { message }
            return DirectDownloadResult(false, message)
        }

        // check to see if target file exists
        if (fileSystem.exists(targetFile)) {
            if (directDownloadRequest.overwriteExisting) {
                try {
                    fileSystem.delete(targetFile)
                } catch (expected: Exception) {
                    val message = "Failed to delete existing target file: [${targetFile}]  message: [${expected.message}]"
                    Logger.e(expected) { message }
                    return DirectDownloadResult(false, message)
                }
            } else {
                return DirectDownloadResult(false, "Failed download to target file...  target file already exists: [${targetFile}]  (overwriteExisting == false)")
            }
        }

        // if we get to this point... all is well! (target directory exists, and target file does NOT yet exist)
        return null
    }

    private fun executeDownload(httpClient: OkHttpClient, directDownloadRequest: DirectDownloadRequest): DirectDownloadResult {
        val fileSystem = directDownloadRequest.fileSystem
        val targetFile = directDownloadRequest.targetFile

        // create request
        val requestBuilder = Request.Builder()
            .url(directDownloadRequest.downloadUrl)

        // add any custom headers
        directDownloadRequest.customHeaders?.forEach { header ->
            requestBuilder.addHeader(header.name, header.value)
        }

        // build the request
        val okhttpRequest = requestBuilder.build()

        // download NOW
        val startMs = System.currentTimeMillis()

        Logger.d { "Direct Downloading [${directDownloadRequest.downloadUrl}] -> [${directDownloadRequest.targetFile}]" }
        val directDownloadResult: DirectDownloadResult = httpClient.newCall(okhttpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                return@use DirectDownloadResult(false, "Failed to download code: ${response.code}", response.code)
            }

            val body = response.body ?: return@use DirectDownloadResult(false, "Download Request body == null", response.code)

            // write body to file
            fileSystem.write(targetFile) {
                writeBodyToBufferedSink(directDownloadRequest, body, this) ?: DirectDownloadResult(true, code = response.code)
            }
        }

        val totalDownloadMs = System.currentTimeMillis() - startMs
        Logger.d { "Direct Download Finished ${totalDownloadMs}ms [${directDownloadResult.code}] [${directDownloadRequest.downloadUrl}] -> [${directDownloadRequest.targetFile}]" }

        // notify observers
        _progressStateFlow.value = DirectDownloadProgress.DownloadComplete(directDownloadResult.success, directDownloadResult.message)

        return directDownloadResult
    }

    private fun writeBodyToBufferedSink(directDownloadRequest: DirectDownloadRequest, body: ResponseBody, buffer: BufferedSink): DirectDownloadResult? {
        try {
            buffer.writeAll(body.source())
        } catch (expected: IllegalStateException) {
            return when {
                expected.message == "closed" && cancelRequested -> {
                    val message = "Direct Download Cancelled for ${directDownloadRequest.targetFile}"
                    Logger.w { message }
                    // do nothing (expected)
                    DirectDownloadResult(false, message)
                }
                else -> {
                    val message = "Failed to download file [${directDownloadRequest.targetFile.name}]"
                    Logger.e(expected) { message }
                    DirectDownloadResult(false, message)
                }
            }
        } catch (expected: Exception) {
            val message = "Failed to download file [${directDownloadRequest.targetFile.name}]"
            Logger.e(expected) { message }
            return DirectDownloadResult(false, message)
        }

        return null // Success (no errors)
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
                    val downloadComplete = bytesRead == -1L // read() returns the number of bytes read, or -1 if this source is exhausted.

                    // update totalBytesRead
                    totalBytesRead += if (!downloadComplete) bytesRead else 0

                    // report progress
//                    reportProgress(downloadComplete)
                    val progress = DirectDownloadProgress.Downloading(totalBytesRead, responseBody.contentLength())
                    _progressStateFlow.value = progress

                    return bytesRead
                }

                private fun reportProgress(downloadComplete: Boolean) {
                    when {
                        downloadComplete -> {
                            _progressStateFlow.value = DirectDownloadProgress.DownloadComplete(true)
                        }
                        else -> {
                            val progress = DirectDownloadProgress.Downloading(totalBytesRead, responseBody.contentLength())
                            _progressStateFlow.value = progress
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val MAX_PROGRESS = 100
    }
}