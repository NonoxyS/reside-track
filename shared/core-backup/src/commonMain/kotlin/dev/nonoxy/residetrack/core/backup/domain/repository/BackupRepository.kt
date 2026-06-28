package dev.nonoxy.residetrack.core.backup.domain.repository

import dev.nonoxy.residetrack.core.backup.domain.model.Backup

/**
 * Export/import of the whole database as a portable JSON backup. Only domain types cross this
 * boundary — the on-disk file format ([dev.nonoxy.residetrack.core.backup.data.model] DTOs) never
 * leaks out. Import is a two-step: [parse] validates a picked file (no DB writes) so the caller can
 * confirm with the user, then [restore] replaces everything atomically.
 */
interface BackupRepository {

    /** Reads the current database into a JSON string ready to be written to a user-picked file. */
    suspend fun export(): Result<String>

    /**
     * Parses and validates [rawJson] from a picked file into a [Backup] without touching the
     * database. Fails on malformed JSON or an incompatible format version.
     */
    suspend fun parse(rawJson: String): Result<Backup>

    /** Replaces the entire database with [backup] atomically (rolls back on failure). */
    suspend fun restore(backup: Backup): Result<Unit>
}
