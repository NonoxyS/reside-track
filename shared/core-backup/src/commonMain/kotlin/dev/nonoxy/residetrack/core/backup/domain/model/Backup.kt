package dev.nonoxy.residetrack.core.backup.domain.model

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
