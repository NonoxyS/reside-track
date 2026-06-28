package dev.nonoxy.residetrack.core.backup.data.gateway

import dev.nonoxy.residetrack.core.backup.domain.model.Backup
import dev.nonoxy.residetrack.core.backup.domain.model.BackupRoom
import dev.nonoxy.residetrack.core.backup.domain.model.BackupStudent
import dev.nonoxy.residetrack.core.database.dao.RoomDao
import dev.nonoxy.residetrack.core.database.entities.RoomEntity
import dev.nonoxy.residetrack.core.database.entities.StudentEntity
import dev.nonoxy.residetrack.core.database.relations.RoomWithStudents

internal class RoomBackupGateway(
    private val roomDao: RoomDao,
) : BackupGateway {

    override suspend fun loadAll(): Backup =
        Backup(rooms = roomDao.getAllRoomsWithStudents().map { it.toBackupRoom() })

    override suspend fun replaceAll(backup: Backup) {
        val rooms = backup.rooms.map { room ->
            RoomEntity(
                floorNumber = room.floorNumber,
                roomNumber = room.roomNumber,
                bedsCount = room.bedsCount,
            )
        }
        val studentsByRoom = backup.rooms.map { room ->
            room.students.map { student ->
                StudentEntity(
                    roomId = 0,
                    streamNumber = student.streamNumber,
                    checkInDateEpochMillis = student.checkInEpochMillis,
                    checkOutDateEpochMillis = student.checkOutEpochMillis,
                )
            }
        }
        roomDao.replaceAllRoomsWithStudents(rooms = rooms, studentsByRoom = studentsByRoom)
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
