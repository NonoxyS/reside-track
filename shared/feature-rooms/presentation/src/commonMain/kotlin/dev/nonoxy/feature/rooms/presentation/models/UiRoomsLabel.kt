package dev.nonoxy.feature.rooms.presentation.models

sealed interface UiRoomsLabel {
    data object NavigateToAddRoomScreen : UiRoomsLabel
    data class NavigateToManageStudentsExistingRoom(val roomId: String) : UiRoomsLabel
}
