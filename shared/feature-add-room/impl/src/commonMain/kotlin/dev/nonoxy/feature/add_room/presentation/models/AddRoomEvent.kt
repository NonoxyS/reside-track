package dev.nonoxy.feature.add_room.presentation.models

internal sealed interface AddRoomEvent {

    class OnFloorNumberInputValueChange(val floorNumber: String) : AddRoomEvent
    class OnFloorNumberSelect(val floorNumber: Int) : AddRoomEvent

    class OnRoomNumberInputValueChange(val roomNumber: String) : AddRoomEvent

    class OnBedsCountInputValueChange(val bedsCount: String) : AddRoomEvent
    class OnBedsCountSelect(val bedsCount: Int) : AddRoomEvent

    object OnCreateRoomClick : AddRoomEvent
    object OnCancelClick : AddRoomEvent

    object OnToggleFloorInput : AddRoomEvent
    object OnToggleBedsInput : AddRoomEvent
}
