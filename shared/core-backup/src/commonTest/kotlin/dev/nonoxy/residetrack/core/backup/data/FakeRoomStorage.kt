package dev.nonoxy.residetrack.core.backup.data

import dev.nonoxy.residetrack.core.database.entities.RoomEntity
import dev.nonoxy.residetrack.core.database.entities.StudentEntity
import dev.nonoxy.residetrack.core.database.relations.RoomWithStudents
import dev.nonoxy.residetrack.core.database.storage.RoomStorage
import kotlinx.coroutines.flow.Flow

internal class FakeRoomStorage(
    private val stored: List<RoomWithStudents> = emptyList(),
) : RoomStorage {

    var replacedRooms: List<RoomEntity>? = null
        private set
    var replacedStudentsByRoom: List<List<StudentEntity>>? = null
        private set

    override suspend fun getAllRoomsWithStudents(): List<RoomWithStudents> = stored

    override suspend fun replaceAllRoomsWithStudents(
        rooms: List<RoomEntity>,
        studentsByRoom: List<List<StudentEntity>>,
    ) {
        replacedRooms = rooms
        replacedStudentsByRoom = studentsByRoom
    }

    override fun observeAllRoomsWithStudents(): Flow<List<RoomWithStudents>> = unused()
    override suspend fun getRoomWithStudentsById(roomId: Long): RoomWithStudents? = unused()
    override suspend fun getRoomWithStudentsByRoomNumber(roomNumber: Int): RoomWithStudents? = unused()
    override suspend fun getRoomsCount(): Int = unused()
    override suspend fun insertRooms(rooms: List<RoomEntity>) = unused()
    override suspend fun saveRoomWithStudents(room: RoomEntity, students: List<StudentEntity>): Long = unused()
    override suspend fun updateRoomMetadata(
        roomId: Long,
        floorNumber: Int,
        roomNumber: Int,
        bedsCount: Int,
    ) = unused()
    override suspend fun deleteRoomWithStudents(roomId: Long) = unused()

    private fun unused(): Nothing = error("not used in backup tests")
}
