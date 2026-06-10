package dev.nonoxy.residetrack.feature.room_editor.presentation.models

data class UiRoomParams(
    val floorNumber: String,
    val roomNumber: String,
    val bedsCount: String,
    val roomNumberError: Boolean,
)
