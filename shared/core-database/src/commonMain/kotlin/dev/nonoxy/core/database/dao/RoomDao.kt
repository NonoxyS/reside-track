package dev.nonoxy.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.nonoxy.core.database.entities.RoomEntity
import dev.nonoxy.core.database.relations.RoomWithStudents

@Dao
interface RoomDao {

    @Query("SELECT * FROM rooms")
    suspend fun getAllRooms(): List<RoomEntity>

    @Query("SELECT * FROM rooms WHERE id = :roomId")
    suspend fun getRoomById(roomId: Long): RoomEntity?

    @Insert
    suspend fun insertRoom(room: RoomEntity)

    @Update
    suspend fun updateRoom(room: RoomEntity)

    @Delete
    suspend fun deleteRoom(room: RoomEntity)

    @Transaction
    @Query("SELECT * FROM rooms WHERE id = :roomId")
    suspend fun getRoomWithStudents(roomId: Long): RoomWithStudents?

    @Transaction
    @Query("SELECT * FROM rooms")
    suspend fun getAllRoomsWithStudents(): List<RoomWithStudents>

    @Query("SELECT * FROM rooms WHERE bedsNumber > 0")
    suspend fun getRoomsWithAvailableBeds(): List<RoomEntity>

    @Query("SELECT * FROM rooms WHERE floorNumber = :floor")
    suspend fun getRoomsByFloor(floor: Int): List<RoomEntity>
}
