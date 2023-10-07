package org.dbtools.android.commons.ext

import com.google.firebase.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Date

fun Map<*, *>.requireString(key: String): String = getString(key) ?: error("Value for key [$key] cannot be null/missing")

fun Map<*, *>.getString(key: String): String? {
    val data = this[key] ?: return null
    return when (data) {
        is String -> data
        else -> error("data for key [$key] is NOT a String.  Data: [${data::class.java}]")
    }
}

fun Map<*, *>.requireBoolean(key: String): Boolean = getBoolean(key) ?: error("Value for key [$key] cannot be null/missing")

fun Map<*, *>.getBoolean(key: String): Boolean? {
    val data = this[key] ?: return null
    return when (data) {
        is Boolean -> data
        else -> error("data for key [$key] is NOT a Boolean.  Data: [${data::class.java}]")
    }
}

fun Map<*, *>.requireInt(key: String): Int = getInt(key) ?: error("Value for key [$key] cannot be null/missing")

fun Map<*, *>.getInt(key: String): Int? {
    val data = this[key] ?: return null
    return when (data) {
        is Int -> data
        is Long -> data.toInt()
        else -> error("data for key [$key] is NOT a Int.  Data: [${data::class.java}]")
    }
}

fun Map<*, *>.requireFloat(key: String): Float = getFloat(key) ?: error("Value for key [$key] cannot be null/missing")

fun Map<*, *>.getFloat(key: String): Float? {
    val data = this[key] ?: return null
    return when (data) {
        is Float -> data
        is Double -> data.toFloat()
        else -> error("data for key [$key] is NOT a Float.  Data: [${data::class.java}]")
    }
}

fun Map<*, *>.requireTimestamp(key: String): Timestamp = getTimestamp(key) ?: error("Value for key [$key] cannot be null/missing")
fun Map<*, *>.getTimestamp(key: String): Timestamp? {
    val data = this[key] ?: return null
    return when (data) {
        is Timestamp -> data
        else -> error("data for key [$key] is NOT a Timestamp.  Data: [${data::class.java}]")
    }
}

fun Map<*, *>.requireDate(key: String): Date = getDate(key) ?: error("Value for key [$key] cannot be null/missing")
fun Map<*, *>.getDate(key: String): Date? = getTimestamp(key)?.toDate()

fun Map<*, *>.requireOffsetDateTime(key: String): OffsetDateTime = getOffsetDateTime(key) ?: error("Value for key [$key] cannot be null/missing")
fun Map<*, *>.getOffsetDateTime(key: String): OffsetDateTime? = getTimestamp(key)?.toDate()?.toInstant()?.atOffset(ZoneOffset.UTC)

fun <T> Map<*, *>.getType(key: String, map: (Any) -> T): T? {
    val data = this[key] ?: return null
    return map(data)
}
