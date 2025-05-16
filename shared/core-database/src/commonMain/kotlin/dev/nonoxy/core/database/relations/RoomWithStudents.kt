package dev.nonoxy.core.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import dev.nonoxy.core.database.entities.RoomEntity
import dev.nonoxy.core.database.entities.StudentEntity

data class RoomWithStudents(
    @Embedded val room: RoomEntity,

    @Relation(parentColumn = "id", entityColumn = "roomId")
    val students: List<StudentEntity>
)
