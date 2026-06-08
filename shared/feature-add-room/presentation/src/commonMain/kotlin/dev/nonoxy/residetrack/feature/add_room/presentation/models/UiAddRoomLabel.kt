package dev.nonoxy.residetrack.feature.add_room.presentation.models

sealed interface UiAddRoomLabel {
    data object CloseScreen : UiAddRoomLabel
    data object NavigateToManageStudentsDraftRoom : UiAddRoomLabel
    data class ShowSuccess(val message: String) : UiAddRoomLabel
    data class ShowError(val message: String) : UiAddRoomLabel
}
