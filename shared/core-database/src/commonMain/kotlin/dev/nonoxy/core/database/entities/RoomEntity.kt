package dev.nonoxy.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val floorNumber: Int,
    val roomNumber: Int,
    val bedsCount: Int,
)
