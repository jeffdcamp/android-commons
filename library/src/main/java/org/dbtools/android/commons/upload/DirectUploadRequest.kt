package org.dbtools.android.commons.upload

import okhttp3.MediaType
import okio.FileSystem
import okio.Path
import java.util.UUID

data class DirectUploadRequest (
    val uploadUrl: String,
    val fileSystem: FileSystem,
    val sourceFile: Path,
    val multiPartFormPartName: String = "file",
    val contentType: MediaType? = null,
    val id: String = UUID.randomUUID().toString(),
    val customHeaders: List<DirectUploadHeader>? = null,
    val formParts: List<DirectUploadFormPart>? = null,
    val getResponseBodyAsString: Boolean = true
)

data class DirectUploadHeader(
    val name: String,
    val value: String
)

data class DirectUploadFormPart(
    val name: String,
    val value: String
)
