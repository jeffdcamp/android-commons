package org.dbtools.android.commons.ext

import androidx.lifecycle.SavedStateHandle

/**
 * Use when you wish to receive a non-null default value from the [SavedStateHandle]
 * when a saved state value does not yet exist for the given key.
 */
inline fun <reified T> SavedStateHandle.get(key: String, default: T): T {
    return get(key) ?: default
}