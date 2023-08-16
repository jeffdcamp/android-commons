package org.dbtools.android.commons.log

/**
 * Non-fatal exception for use with Logger and Crashlytics
 */
class LoggerNonFatalCrashLogException(message: String) : RuntimeException(message)
