package dev.nonoxy.residetrack.feature.add_room.api.store

sealed interface AddRoomErrorKind {
    data object UnknownError : AddRoomErrorKind
    data object SaveFailed : AddRoomErrorKind
    data class RoomAlreadyExists(val roomNumber: Int, val floorNumber: Int) : AddRoomErrorKind
    data object FloorNumberRequired : AddRoomErrorKind
    data object RoomNumberRequired : AddRoomErrorKind
    data object BedsCountRequired : AddRoomErrorKind
}
