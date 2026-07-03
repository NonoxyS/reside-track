package dev.nonoxy.residetrack.common.utils

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

// Check-in/out are calendar days without a time component, persisted as UTC-midnight epoch
// millis. Canonicalizing on UTC keeps the stored day equal to the picked day in every timezone
// and portable across a backup exported on another device. The Material date picker and
// RoomEditor already use the same UTC convention.
fun Long.epochMillisToUtcDate(): LocalDate =
    Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(timeZone = TimeZone.UTC)
        .date

fun LocalDate.toUtcStartOfDayMillis(): Long =
    atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

// A real instant resolved to the user's local calendar day — used for "today", never for
// interpreting a stored calendar date (those go through the UTC helpers above).
fun Instant.toLocalDate(): LocalDate =
    toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
        .date

val currentLocalDate: LocalDate
    get() = Clock.System.now().toLocalDate()
