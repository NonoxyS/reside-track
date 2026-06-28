package dev.nonoxy.residetrack.feature.rooms.impl.data

import dev.nonoxy.residetrack.core.backup.domain.model.Backup
import dev.nonoxy.residetrack.core.backup.domain.repository.BackupRepository

internal class FakeBackupRepository(
    private val exportResult: Result<String> = Result.success("{}"),
    private val parseResult: Result<Backup> = Result.success(Backup(rooms = emptyList())),
    private val restoreResult: Result<Unit> = Result.success(Unit),
) : BackupRepository {

    var restoreCalledWith: Backup? = null
        private set

    override suspend fun export(): Result<String> = exportResult

    override suspend fun parse(rawJson: String): Result<Backup> = parseResult

    override suspend fun restore(backup: Backup): Result<Unit> {
        restoreCalledWith = backup
        return restoreResult
    }
}
