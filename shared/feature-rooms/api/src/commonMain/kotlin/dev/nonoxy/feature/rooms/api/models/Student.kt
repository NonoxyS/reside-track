package dev.nonoxy.feature.rooms.api.models

import kotlinx.datetime.LocalDate

data class Student(
    val id: Long,
    val streamNumber: Int,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val isCheckOutDateNearOrExpired: Boolean
)
