package dev.nonoxy.residetrack.core.backup.data.gateway

import dev.nonoxy.residetrack.core.backup.domain.model.Backup

/**
 * Persistence port for backup, speaking domain [Backup] only. Exists so [BackupRepositoryImpl]
 * stays free of Room types and is unit-testable with a fake — the concrete [RoomBackupGateway]
 * wraps the DAO and owns the entity↔domain mapping.
 */
internal interface BackupGateway {

    suspend fun loadAll(): Backup

    suspend fun replaceAll(backup: Backup)
}
