package org.dbtools.android.commons.ext

import kotlinx.datetime.DayOfWeek
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.nanoseconds

fun Instant.trimToSeconds(): Instant {
    return minus(nanosecondsOfSecond.nanoseconds)
}

fun Instant.dayOfWeek(timeZone: TimeZone = TimeZone.currentSystemDefault()): DayOfWeek = toLocalDateTime(timeZone).dayOfWeek

fun Instant.nextDayOfWeek(
    dayOfWeek: DayOfWeek,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Instant {
    val daysDiff = dayOfWeek(timeZone).isoDayNumber - dayOfWeek.isoDayNumber
    val daysToAdd = if (daysDiff >= 0) 7 - daysDiff else -daysDiff
    return plus(daysToAdd.days)
}

fun Instant.previousOrSameDayOfWeek(
    dayOfWeek: DayOfWeek,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Instant {
    val daysDiff = dayOfWeek.isoDayNumber - dayOfWeek(timeZone).isoDayNumber
    if (daysDiff == 0) return this

    val daysToAdd = if (daysDiff >= 0) 7 - daysDiff else -daysDiff
    return minus(daysToAdd.days)
}
