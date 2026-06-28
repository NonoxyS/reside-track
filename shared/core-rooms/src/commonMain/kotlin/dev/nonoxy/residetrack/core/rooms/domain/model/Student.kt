package dev.nonoxy.residetrack.core.rooms.domain.model

import kotlinx.datetime.LocalDate

data class Student(
    val id: Long,
    val streamNumber: Int,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate,
    val isCheckOutDateNearOrExpired: Boolean
)
