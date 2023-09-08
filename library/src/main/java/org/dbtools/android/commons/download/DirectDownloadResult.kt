package org.dbtools.android.commons.download

data class DirectDownloadResult(
    val success: Boolean,
    val message: String? = null,
    val code: Int = -1
)