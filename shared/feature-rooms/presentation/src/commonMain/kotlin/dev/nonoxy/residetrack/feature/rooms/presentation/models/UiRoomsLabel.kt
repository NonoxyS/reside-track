package dev.nonoxy.residetrack.feature.rooms.presentation.models

sealed interface UiRoomsLabel {
    data object NavigateToAddRoomScreen : UiRoomsLabel
    data class NavigateToRoomEditorExistingRoom(val roomId: String) : UiRoomsLabel
}
