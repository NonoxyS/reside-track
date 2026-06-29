package dev.nonoxy.residetrack.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.nonoxy.residetrack.core.database.entities.RoomEntity
import dev.nonoxy.residetrack.core.database.entities.StudentEntity
import dev.nonoxy.residetrack.core.database.relations.RoomWithStudents
import kotlinx.coroutines.flow.Flow

@Dao
internal abstract class RoomDao {

    @Query("SELECT COUNT(*) FROM rooms")
    abstract suspend fun getRoomsCount(): Int

    @Insert(onConflict = REPLACE)
    abstract suspend fun insertRoom(room: RoomEntity): Long

    @Insert
    abstract suspend fun insertRooms(rooms: List<RoomEntity>)

    @Update
    abstract suspend fun updateRoom(room: RoomEntity)

    @Delete
    abstract suspend fun deleteRoom(room: RoomEntity)

    @Transaction
    @Query("SELECT * FROM rooms WHERE id = :roomId")
    abstract suspend fun getRoomWithStudentsById(roomId: Long): RoomWithStudents?

    @Transaction
    @Query("SELECT * FROM rooms WHERE roomNumber = :roomNumber")
    abstract suspend fun getRoomWithStudentsByRoomNumber(roomNumber: Int): RoomWithStudents?

    @Transaction
    @Query("SELECT * FROM rooms")
    abstract suspend fun getAllRoomsWithStudents(): List<RoomWithStudents>

    @Transaction
    @Query("SELECT * FROM rooms")
    abstract fun observeAllRoomsWithStudents(): Flow<List<RoomWithStudents>>

    @Query("DELETE FROM students WHERE roomId = :roomId")
    protected abstract suspend fun deleteStudentsByRoomId(roomId: Long)

    @Query("DELETE FROM rooms WHERE id = :roomId")
    protected abstract suspend fun deleteRoomById(roomId: Long)

    @Query("DELETE FROM rooms")
    protected abstract suspend fun deleteAllRooms()

    @Query(
        "UPDATE rooms SET floorNumber = :floorNumber, roomNumber = :roomNumber, " +
            "bedsCount = :bedsCount WHERE id = :roomId"
    )
    abstract suspend fun updateRoomMetadata(
        roomId: Long,
        floorNumber: Int,
        roomNumber: Int,
        bedsCount: Int,
    )

    /** Explicit student delete keeps this correct even if SQLite FK enforcement is off. */
    @Transaction
    open suspend fun deleteRoomWithStudents(roomId: Long) {
        deleteStudentsByRoomId(roomId)
        deleteRoomById(roomId)
    }

    @Insert
    protected abstract suspend fun insertStudents(students: List<StudentEntity>)

    /**
     * [room].id == 0 inserts and returns the new id; otherwise updates in place. Students are
     * replaced, each one's roomId rewritten to the persisted room.
     */
    @Transaction
    open suspend fun saveRoomWithStudents(
        room: RoomEntity,
        students: List<StudentEntity>,
    ): Long {
        val roomId = if (room.id == 0L) {
            insertRoom(room)
        } else {
            updateRoom(room)
            room.id
        }
        deleteStudentsByRoomId(roomId)
        if (students.isNotEmpty()) {
            insertStudents(students.map { it.copy(roomId = roomId) })
        }
        return roomId
    }

    /** [studentsByRoom] is parallel to [rooms]. Ids are regenerated, so a backup never needs stable ids. */
    @Transaction
    open suspend fun replaceAllRoomsWithStudents(
        rooms: List<RoomEntity>,
        studentsByRoom: List<List<StudentEntity>>,
    ) {
        require(rooms.size == studentsByRoom.size) {
            "rooms (${rooms.size}) and studentsByRoom (${studentsByRoom.size}) must be parallel lists"
        }
        deleteAllRooms()
        rooms.forEachIndexed { index, room ->
            val roomId = insertRoom(room.copy(id = 0))
            val students = studentsByRoom[index]
            if (students.isNotEmpty()) {
                insertStudents(students.map { it.copy(id = 0, roomId = roomId) })
            }
        }
    }
}
