package dev.nonoxy.feature.rooms.models

import kotlinx.datetime.LocalDate

class Student(
    val id: Long,
    val streamNumber: Int,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate
)
