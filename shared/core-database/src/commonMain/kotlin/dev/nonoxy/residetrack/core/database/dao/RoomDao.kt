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
abstract class RoomDao {

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

    /** Deletes the room and its students atomically. Explicit student delete keeps it
     *  correct even if SQLite foreign-key enforcement is off. */
    @Transaction
    open suspend fun deleteRoomWithStudents(roomId: Long) {
        deleteStudentsByRoomId(roomId)
        deleteRoomById(roomId)
    }

    @Insert
    protected abstract suspend fun insertStudents(students: List<StudentEntity>)

    /**
     * Replaces the room's student set atomically. If [room].id == 0, a new row is inserted and the
     * generated id is returned; otherwise the existing row is updated in place (no CASCADE).
     * All existing students in this room are deleted, then [students] (with their roomId rewritten
     * to the persisted room id) are inserted.
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

    /**
     * Wipes every room (and, via CASCADE, every student) and re-inserts [rooms] with their students
     * atomically — the restore half of backup import. Each entry in [studentsByRoom] holds the
     * students for the room at the same index in [rooms]; ids are regenerated and each student's
     * roomId is rewritten to its freshly inserted room, so the backup file never needs stable ids.
     */
    @Transaction
    open suspend fun replaceAllRoomsWithStudents(
        rooms: List<RoomEntity>,
        studentsByRoom: List<List<StudentEntity>>,
    ) {
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
