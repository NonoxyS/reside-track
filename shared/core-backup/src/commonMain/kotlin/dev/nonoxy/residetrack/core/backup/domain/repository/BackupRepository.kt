package dev.nonoxy.residetrack.core.backup.domain.repository

import dev.nonoxy.residetrack.core.backup.domain.model.Backup

interface BackupRepository {

    suspend fun export(): Result<String>

    /**
     * Validates a picked file without touching the database, so the caller can confirm with the
     * user before [restore] replaces everything.
     */
    suspend fun parse(rawJson: String): Result<Backup>

    suspend fun restore(backup: Backup): Result<Unit>
}
