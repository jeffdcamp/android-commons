@file:Suppress("unused")

package org.dbtools.android.commons.coroutine

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope

/**
 * Replaces GlobalScope
 */
val ProcessScope: CoroutineScope get() = ProcessLifecycleOwner.get().lifecycleScope
