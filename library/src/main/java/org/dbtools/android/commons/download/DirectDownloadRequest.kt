package org.dbtools.android.commons.download

import okio.FileSystem
import okio.Path
import java.util.UUID

data class DirectDownloadRequest (
    val downloadUrl: String,
    val fileSystem: FileSystem,
    val targetFile: Path,
    val id: String = UUID.randomUUID().toString(),
    val overwriteExisting: Boolean = true,
    val customHeaders: List<DirectDownloadHeader>? = null
)

data class DirectDownloadHeader(
    val name: String,
    val value: String
)