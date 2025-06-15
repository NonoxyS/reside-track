package dev.nonoxy.feature.add_room.presentation.models

internal sealed interface AddRoomAction {

    object CloseScreen : AddRoomAction

    class ShowSuccessMessage(val message: String) : AddRoomAction

    class ShowErrorMessage(val message: String) : AddRoomAction
}
