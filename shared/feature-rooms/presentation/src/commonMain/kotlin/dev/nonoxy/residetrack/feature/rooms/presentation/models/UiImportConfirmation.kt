package dev.nonoxy.residetrack.feature.rooms.presentation.models

/**
 * Counts shown in the import confirmation dialog. Both the incoming backup and the data about to be
 * destroyed are exposed so the user sees the full consequence of a destructive replace-all restore.
 */
data class UiImportConfirmation(
    val roomCount: Int,
    val studentCount: Int,
    val currentRoomCount: Int,
    val currentStudentCount: Int,
)
