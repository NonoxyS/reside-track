package dev.nonoxy.residetrack.common.utils

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Long.toLocalDate(): LocalDate {
    return Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
        .date
}

fun Instant.toLocalDate(): LocalDate {
    return this.toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
        .date
}

val currentLocalDate: LocalDate
    get() = Clock.System.now().toLocalDate()
