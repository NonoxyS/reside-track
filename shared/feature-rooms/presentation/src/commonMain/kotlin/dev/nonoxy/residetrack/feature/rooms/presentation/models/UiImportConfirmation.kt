package dev.nonoxy.residetrack.feature.rooms.presentation.models

data class UiImportConfirmation(
    val roomCount: Int,
    val studentCount: Int,
    // current* = data the replace-all will destroy; shown so the user sees the full consequence.
    val currentRoomCount: Int,
    val currentStudentCount: Int,
)
