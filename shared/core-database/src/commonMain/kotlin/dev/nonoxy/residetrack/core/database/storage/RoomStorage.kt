package dev.nonoxy.residetrack.core.database.storage

import dev.nonoxy.residetrack.core.database.entities.RoomEntity
import dev.nonoxy.residetrack.core.database.entities.StudentEntity
import dev.nonoxy.residetrack.core.database.relations.RoomWithStudents
import kotlinx.coroutines.flow.Flow

interface RoomStorage {

    fun observeAllRoomsWithStudents(): Flow<List<RoomWithStudents>>

    suspend fun getAllRoomsWithStudents(): List<RoomWithStudents>

    suspend fun getRoomWithStudentsById(roomId: Long): RoomWithStudents?

    suspend fun getRoomWithStudentsByRoomNumber(roomNumber: Int): RoomWithStudents?

    suspend fun getRoomsCount(): Int

    suspend fun insertRooms(rooms: List<RoomEntity>)

    suspend fun saveRoomWithStudents(room: RoomEntity, students: List<StudentEntity>): Long

    suspend fun updateRoomMetadata(roomId: Long, floorNumber: Int, roomNumber: Int, bedsCount: Int)

    suspend fun deleteRoomWithStudents(roomId: Long)

    suspend fun replaceAllRoomsWithStudents(
        rooms: List<RoomEntity>,
        studentsByRoom: List<List<StudentEntity>>,
    )
}
