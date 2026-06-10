package dev.nonoxy.residetrack.core.rooms.repository

import dev.nonoxy.residetrack.core.rooms.models.Room
import kotlinx.coroutines.flow.Flow

interface RoomsRepository {

    /** Reactive stream of all rooms; re-emits whenever the rooms/students tables change. */
    fun observeRooms(): Flow<List<Room>>

    suspend fun getAllRooms(): Result<List<Room>>
    suspend fun getRoomsByFloor(floorNumber: Int): Result<List<Room>>
    suspend fun getRoomByNumber(roomNumber: Int): Result<Room?>
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

    suspend fun saveDraftRoom(room: Room): Result<Unit>
    suspend fun getDraftRoom(): Result<Room?>
    suspend fun clearDraftRoom(): Result<Unit>
    fun observeDraftRoom(): Flow<Room?>
}
