package org.dbtools.android.commons.log

/**
 * Non-fatal exception for use with Logger and Crashlytics
 */
class CrashLogException(message: String = "") : RuntimeException(message)
