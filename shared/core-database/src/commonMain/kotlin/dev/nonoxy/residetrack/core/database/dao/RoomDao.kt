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

    @Query("SELECT * FROM rooms")
    abstract suspend fun getAllRooms(): List<RoomEntity>

    @Query("SELECT * FROM rooms WHERE id = :roomId")
    abstract suspend fun getRoomById(roomId: Long): RoomEntity?

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

    @Transaction
    @Query("SELECT * FROM rooms WHERE floorNumber = :floor")
    abstract suspend fun getRoomsWithStudentsByFloor(floor: Int): List<RoomWithStudents>

    @Query("SELECT * FROM rooms WHERE bedsCount > 0")
    abstract suspend fun getRoomsWithAvailableBeds(): List<RoomEntity>

    @Query("SELECT * FROM rooms WHERE floorNumber = :floor")
    abstract suspend fun getRoomsByFloor(floor: Int): List<RoomEntity>

    @Query("DELETE FROM students WHERE roomId = :roomId")
    protected abstract suspend fun deleteStudentsByRoomId(roomId: Long)

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
}
