package dev.nonoxy.residetrack.core.backup.data

import dev.nonoxy.residetrack.core.backup.data.gateway.BackupGateway
import dev.nonoxy.residetrack.core.backup.domain.model.Backup

internal class FakeBackupGateway(
    private val stored: Backup = Backup(rooms = emptyList()),
) : BackupGateway {

    var restoredWith: Backup? = null
        private set

    override suspend fun loadAll(): Backup = stored

    override suspend fun replaceAll(backup: Backup) {
        restoredWith = backup
    }
}
