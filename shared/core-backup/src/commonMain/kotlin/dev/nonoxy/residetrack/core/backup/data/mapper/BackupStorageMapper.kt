package dev.nonoxy.residetrack.core.backup.data.mapper

import dev.nonoxy.residetrack.core.backup.domain.model.Backup
import dev.nonoxy.residetrack.core.backup.domain.model.BackupRoom
import dev.nonoxy.residetrack.core.backup.domain.model.BackupStudent
import dev.nonoxy.residetrack.core.database.entities.RoomEntity
import dev.nonoxy.residetrack.core.database.entities.StudentEntity
import dev.nonoxy.residetrack.core.database.relations.RoomWithStudents

internal fun List<RoomWithStudents>.toBackup(): Backup =
    Backup(rooms = map { row -> row.toBackupRoom() })

internal fun Backup.toRoomRows(): List<RoomEntity> = rooms.map { room ->
    RoomEntity(
        floorNumber = room.floorNumber,
        roomNumber = room.roomNumber,
        bedsCount = room.bedsCount,
    )
}

internal fun Backup.toStudentRowsByRoom(): List<List<StudentEntity>> = rooms.map { room ->
    room.students.map { student ->
        StudentEntity(
            roomId = 0,
            streamNumber = student.streamNumber,
            checkInDateEpochMillis = student.checkInEpochMillis,
            checkOutDateEpochMillis = student.checkOutEpochMillis,
        )
    }
}

private fun RoomWithStudents.toBackupRoom(): BackupRoom = BackupRoom(
    floorNumber = room.floorNumber,
    roomNumber = room.roomNumber,
    bedsCount = room.bedsCount,
    students = students.map { entity ->
        BackupStudent(
            streamNumber = entity.streamNumber,
            checkInEpochMillis = entity.checkInDateEpochMillis,
            checkOutEpochMillis = entity.checkOutDateEpochMillis,
        )
    },
)
