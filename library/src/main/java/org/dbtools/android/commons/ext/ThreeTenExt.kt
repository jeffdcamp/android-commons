package org.dbtools.android.commons.ext

import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Date

fun Date.toOffsetDateTime(): OffsetDateTime = toInstant().atOffset(ZoneOffset.UTC)
