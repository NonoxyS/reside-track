package dev.nonoxy.residetrack.core.backup.data.model

import kotlinx.serialization.Serializable

/** Current backup file format version. Bumped whenever the on-disk schema changes. */
internal const val BACKUP_FORMAT_VERSION = 1

/**
 * On-disk JSON shape of a backup. Lives in the data layer and never crosses the repository
 * boundary — [version]/[exportedAtEpochMillis] are file concerns the domain must not know about.
 * Room/student ids are intentionally absent: restore is replace-all, ids are regenerated and the
 * room↔student link is carried by nesting.
 */
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
