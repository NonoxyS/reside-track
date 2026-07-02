package dev.nonoxy.residetrack.core.rooms.domain.repository

import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import kotlinx.coroutines.flow.Flow

interface RoomsRepository {

    /** Reactive stream of all rooms; re-emits whenever the rooms/students tables change. */
    fun observeRooms(): Flow<List<Room>>

    suspend fun getAllRooms(): Result<List<Room>>
    suspend fun getRoomById(roomId: Long): Result<Room?>

    suspend fun saveRoom(room: Room): Result<Long>

    /** Updates only floor/room/beds for [roomId]; leaves students untouched. */
    suspend fun updateRoomMetadata(
        roomId: Long,
        floorNumber: Int,
        roomNumber: Int,
        bedsCount: Int,
    ): Result<Unit>

    /** Deletes the room and its students. */
    suspend fun deleteRoom(roomId: Long): Result<Unit>
}
