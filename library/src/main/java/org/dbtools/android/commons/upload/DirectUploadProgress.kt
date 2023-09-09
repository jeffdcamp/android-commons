@file:Suppress("MemberVisibilityCanBePrivate")

package org.dbtools.android.commons.upload

sealed class DirectUploadProgress {
    data object Enqueued : DirectUploadProgress()
    class Uploading(val totalBytesRead: Long, val contentLength: Long) : DirectUploadProgress() {
        fun calculateProgress(): Long = (100 * totalBytesRead) / contentLength
    }
    class UploadComplete(val success: Boolean, val message: String? = null) : DirectUploadProgress()

    /**
     * Progress based on a range of 0-100
     */
    val progress: Int
        get() {
            return when (this) {
                is Enqueued -> 0
                is Uploading -> calculateProgress().toInt()
                is UploadComplete -> MAX_PROGRESS
            }
        }

    val viewProgress: Float
        get() {
        return when (this) {
            is Enqueued -> 0f
            is Uploading -> calculateProgress() / 100f
            is UploadComplete -> MAX_COMPOSE_PROGRESS
        }
    }

    fun toDebugString(): String {
        return when (this) {
            is Enqueued -> "Enqueued"
            is Uploading -> "${this.calculateProgress()}%"
            is UploadComplete -> "Upload Complete (success: ${this.success}${if (this.message.isNullOrBlank()) "" else "  message: " + this.message})"
        }
    }

    companion object {
        const val MAX_PROGRESS = 100
        const val MAX_COMPOSE_PROGRESS = 1f
    }
}
