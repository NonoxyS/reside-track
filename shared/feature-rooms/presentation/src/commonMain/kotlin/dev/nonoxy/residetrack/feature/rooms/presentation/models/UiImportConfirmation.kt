package dev.nonoxy.residetrack.feature.rooms.presentation.models

/** Counts shown in the import confirmation dialog so the user sees what they are about to restore. */
data class UiImportConfirmation(
    val roomCount: Int,
    val studentCount: Int,
)
