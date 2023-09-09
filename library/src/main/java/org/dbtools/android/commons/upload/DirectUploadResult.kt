package org.dbtools.android.commons.upload

data class DirectUploadResult(
    val success: Boolean,
    val message: String? = null,
    val code: Int = -1,
    val responseBodyString: String? = null
)