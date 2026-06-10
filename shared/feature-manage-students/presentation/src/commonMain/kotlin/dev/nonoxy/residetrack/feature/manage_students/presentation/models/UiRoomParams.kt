package dev.nonoxy.residetrack.feature.manage_students.presentation.models

data class UiRoomParams(
    val floorNumber: String,
    val roomNumber: String,
    val bedsCount: String,
    val roomNumberError: Boolean,
)
