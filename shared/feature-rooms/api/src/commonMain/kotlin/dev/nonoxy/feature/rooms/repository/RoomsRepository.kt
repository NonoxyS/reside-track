package dev.nonoxy.feature.rooms.repository

import dev.nonoxy.feature.rooms.models.Room
import kotlinx.coroutines.flow.Flow

interface RoomsRepository {

    suspend fun getAllRooms(): Result<List<Room>>
    suspend fun getRoomsByFloor(floorNumber: Int): Result<List<Room>>
    suspend fun getRoomByNumber(roomNumber: Int): Result<Room?>
    suspend fun getRoomById(roomId: Long): Result<Room?>

    suspend fun saveRoom(room: Room): Result<Unit>

    suspend fun saveDraftRoom(room: Room): Result<Unit>
    suspend fun getDraftRoom(): Result<Room?>
    suspend fun clearDraftRoom(): Result<Unit>
    fun observeDraftRoom(): Flow<Room?>
}
