package dev.nonoxy.residetrack.core.backup.data.model

import kotlinx.serialization.Serializable

internal const val BACKUP_FORMAT_VERSION = 1

@Serializable
internal data class BackupFileDto(
    val version: Int,
    val exportedAtEpochMillis: Long,
    val rooms: List<BackupRoomDto>,
)

@Serializable
internal data class BackupRoomDto(
    val floorNumber: Int,
    val roomNumber: Int,
    val bedsCount: Int,
    val students: List<BackupStudentDto>,
)

@Serializable
internal data class BackupStudentDto(
    val streamNumber: Int,
    val checkInEpochMillis: Long,
    val checkOutEpochMillis: Long,
)
