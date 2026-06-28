package dev.nonoxy.residetrack.feature.rooms.presentation.models

sealed interface UiRoomsLabel {
    data object NavigateToAddRoomScreen : UiRoomsLabel
    data class NavigateToRoomEditorExistingRoom(val roomId: String) : UiRoomsLabel

    data class SaveBackupFile(val json: String, val suggestedName: String) : UiRoomsLabel
    data object OpenBackupFile : UiRoomsLabel
    data class ShowBackupSuccess(val message: String) : UiRoomsLabel
    data class ShowBackupError(val message: String) : UiRoomsLabel
}
