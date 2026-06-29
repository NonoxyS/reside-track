package dev.nonoxy.residetrack.feature.add_room.api.store

sealed interface AddRoomSuccessKind {
    data class RoomCreated(val roomNumber: Int) : AddRoomSuccessKind
}
