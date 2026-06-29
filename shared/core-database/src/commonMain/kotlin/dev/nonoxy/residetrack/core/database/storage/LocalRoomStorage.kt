package dev.nonoxy.residetrack.core.database.storage

import dev.nonoxy.residetrack.core.database.dao.RoomDao
import dev.nonoxy.residetrack.core.database.entities.RoomEntity
import dev.nonoxy.residetrack.core.database.entities.StudentEntity
import dev.nonoxy.residetrack.core.database.relations.RoomWithStudents
import kotlinx.coroutines.flow.Flow

internal class LocalRoomStorage(
    private val roomDao: RoomDao,
) : RoomStorage {

    override fun observeAllRoomsWithStudents(): Flow<List<RoomWithStudents>> =
        roomDao.observeAllRoomsWithStudents()

    override suspend fun getAllRoomsWithStudents(): List<RoomWithStudents> =
        roomDao.getAllRoomsWithStudents()

    override suspend fun getRoomWithStudentsById(roomId: Long): RoomWithStudents? =
        roomDao.getRoomWithStudentsById(roomId)

    override suspend fun getRoomWithStudentsByRoomNumber(roomNumber: Int): RoomWithStudents? =
        roomDao.getRoomWithStudentsByRoomNumber(roomNumber)

    override suspend fun getRoomsCount(): Int = roomDao.getRoomsCount()

    override suspend fun insertRooms(rooms: List<RoomEntity>) = roomDao.insertRooms(rooms)

    override suspend fun saveRoomWithStudents(room: RoomEntity, students: List<StudentEntity>): Long =
        roomDao.saveRoomWithStudents(room, students)

    override suspend fun updateRoomMetadata(
        roomId: Long,
        floorNumber: Int,
        roomNumber: Int,
        bedsCount: Int,
    ) = roomDao.updateRoomMetadata(roomId, floorNumber, roomNumber, bedsCount)

    override suspend fun deleteRoomWithStudents(roomId: Long) =
        roomDao.deleteRoomWithStudents(roomId)

    override suspend fun replaceAllRoomsWithStudents(
        rooms: List<RoomEntity>,
        studentsByRoom: List<List<StudentEntity>>,
    ) = roomDao.replaceAllRoomsWithStudents(rooms, studentsByRoom)
}
