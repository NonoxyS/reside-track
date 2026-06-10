package dev.nonoxy.residetrack.core.rooms.validation

import dev.nonoxy.residetrack.core.rooms.models.Room

/** Single source of truth for "is this floor+room number already taken by another room". */
object RoomNumberConflict {

    fun exists(
        rooms: List<Room>,
        floorNumber: Int,
        roomNumber: Int,
        excludeRoomId: Long?,
    ): Boolean = rooms.any { room ->
        room.id != excludeRoomId &&
            room.floorNumber == floorNumber &&
            room.roomNumber == roomNumber
    }
}
