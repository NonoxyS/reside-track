package dev.nonoxy.residetrack.core.backup.data.mapper

import dev.nonoxy.residetrack.core.backup.data.model.BackupRoomDto
import dev.nonoxy.residetrack.core.backup.data.model.BackupStudentDto
import dev.nonoxy.residetrack.core.backup.domain.model.Backup
import dev.nonoxy.residetrack.core.backup.domain.model.BackupRoom
import dev.nonoxy.residetrack.core.backup.domain.model.BackupStudent

internal fun Backup.toRoomDtos(): List<BackupRoomDto> = rooms.map { room -> room.toDto() }

internal fun List<BackupRoomDto>.toDomain(): Backup = Backup(rooms = map { dto -> dto.toDomain() })

private fun BackupRoom.toDto(): BackupRoomDto = BackupRoomDto(
    floorNumber = floorNumber,
    roomNumber = roomNumber,
    bedsCount = bedsCount,
    students = students.map { student -> student.toDto() },
)

private fun BackupStudent.toDto(): BackupStudentDto = BackupStudentDto(
    streamNumber = streamNumber,
    checkInEpochMillis = checkInEpochMillis,
    checkOutEpochMillis = checkOutEpochMillis,
)

private fun BackupRoomDto.toDomain(): BackupRoom = BackupRoom(
    floorNumber = floorNumber,
    roomNumber = roomNumber,
    bedsCount = bedsCount,
    students = students.map { dto -> dto.toDomain() },
)

private fun BackupStudentDto.toDomain(): BackupStudent = BackupStudent(
    streamNumber = streamNumber,
    checkInEpochMillis = checkInEpochMillis,
    checkOutEpochMillis = checkOutEpochMillis,
)
