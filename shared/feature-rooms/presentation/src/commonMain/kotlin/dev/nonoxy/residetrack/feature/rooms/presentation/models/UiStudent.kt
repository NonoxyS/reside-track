package dev.nonoxy.residetrack.feature.rooms.presentation.models

data class UiStudent(
    val streamNumber: String,
    val checkInDate: String,
    val checkOutDate: String,
    val isCheckOutDateNearOrExpired: Boolean
)
