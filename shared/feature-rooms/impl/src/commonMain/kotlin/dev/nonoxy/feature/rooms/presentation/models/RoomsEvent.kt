package dev.nonoxy.feature.rooms.presentation.models

internal sealed interface RoomsEvent {

    class OnTabSelect(val tabIndex: Int) : RoomsEvent
    class OnRoomClick(val roomId: Long) : RoomsEvent

    object OnAddRoomClick : RoomsEvent
}
