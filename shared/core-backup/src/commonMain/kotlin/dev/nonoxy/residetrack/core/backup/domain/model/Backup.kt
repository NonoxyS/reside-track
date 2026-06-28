package dev.nonoxy.residetrack.core.backup.domain.model

/**
 * A backup snapshot of the whole database. This is its own bounded context — deliberately separate
 * from the live `Room`/`Student` domain — so the backup format can evolve without dragging the
 * rooms domain along. Dates stay as epoch millis (the persistence representation) because a backup
 * is a raw dump, not a domain view.
 */
data class Backup(
    val rooms: List<BackupRoom>,
) {
    val roomCount: Int get() = rooms.size
    val studentCount: Int get() = rooms.sumOf { room -> room.students.size }
    val isEmpty: Boolean get() = rooms.isEmpty()
}

data class BackupRoom(
    val floorNumber: Int,
    val roomNumber: Int,
    val bedsCount: Int,
    val students: List<BackupStudent>,
)

data class BackupStudent(
    val streamNumber: Int,
    val checkInEpochMillis: Long,
    val checkOutEpochMillis: Long,
)
