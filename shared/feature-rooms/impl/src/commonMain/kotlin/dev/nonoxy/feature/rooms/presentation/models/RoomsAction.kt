package dev.nonoxy.feature.rooms.presentation.models

internal sealed interface RoomsAction {

    object NavigateToAddRoomScreen : RoomsAction

    data class NavigateToManageStudentsExistingRoom(val roomId: String) : RoomsAction
}
