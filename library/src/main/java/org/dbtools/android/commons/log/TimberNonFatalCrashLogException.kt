/*
 * Copyright 2012-2022 Jeff Campbell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dbtools.android.commons.log

import timber.log.Timber

/**
 * Non-fatal exception for use with Timber and Crashlytics
 */
class TimberNonFatalCrashLogException(message: String) : RuntimeException(message) {

    override fun fillInStackTrace(): Throwable {
        super.fillInStackTrace()
        val iterator = stackTrace.iterator()
        val filtered = ArrayList<StackTraceElement>()

        // heading to top of Timber stack trace
        while (iterator.hasNext()) {
            val stackTraceElement = iterator.next()
            if (isTimber(stackTraceElement)) {
                break
            }
        }

        // copy all
        var isReachedApp = false
        while (iterator.hasNext()) {
            val stackTraceElement = iterator.next()
            // skip Timber
            if (!isReachedApp && isTimber(stackTraceElement)) {
                continue
            }
            isReachedApp = true
            filtered.add(stackTraceElement)
        }

        stackTrace = filtered.toTypedArray()
        return this
    }

    private fun isTimber(stackTraceElement: StackTraceElement): Boolean {
        return stackTraceElement.className == Timber::class.java.name
    }
}