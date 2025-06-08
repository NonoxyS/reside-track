package dev.nonoxy.feature.rooms.ui.models

internal data class UiStudent(
    val streamNumber: String,
    val checkInDate: String,
    val checkOutDate: String,
    val isCheckOutDateNearOrExpired: Boolean
)
