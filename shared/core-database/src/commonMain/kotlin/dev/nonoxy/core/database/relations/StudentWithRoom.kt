package dev.nonoxy.core.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.nonoxy.core.database.entities.RoomEntity
import dev.nonoxy.core.database.entities.StudentEntity

data class StudentWithRoom(
    @Embedded val student: StudentEntity,

    @Relation(parentColumn = "roomId", entityColumn = "id")
    val room: RoomEntity
)
