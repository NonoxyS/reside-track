package dev.nonoxy.common.utils

import kotlinx.datetime.Instant
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
