package dev.nonoxy.residetrack.feature.rooms.presentation.models

sealed interface UiRoomsLabel {
    data object NavigateToAddRoomScreen : UiRoomsLabel
    data class NavigateToManageStudentsExistingRoom(val roomId: String) : UiRoomsLabel
}
