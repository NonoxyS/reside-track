package dev.nonoxy.residetrack.core.backup.data

import dev.nonoxy.residetrack.core.backup.data.gateway.BackupGateway
import dev.nonoxy.residetrack.core.backup.data.mapper.toDomain
import dev.nonoxy.residetrack.core.backup.data.mapper.toRoomDtos
import dev.nonoxy.residetrack.core.backup.data.model.BACKUP_FORMAT_VERSION
import dev.nonoxy.residetrack.core.backup.data.model.BackupFileDto
import dev.nonoxy.residetrack.core.backup.domain.model.Backup
import dev.nonoxy.residetrack.core.backup.domain.model.UnsupportedBackupVersionException
import dev.nonoxy.residetrack.core.backup.domain.repository.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.time.Clock

internal class BackupRepositoryImpl(
    private val gateway: BackupGateway,
    private val json: Json,
) : BackupRepository {

    // Serialization is CPU-bound and can be heavy for a large dorm; keep it off the caller's
    // dispatcher. The gateway's Room access already confines its own DB threading.
    override suspend fun export(): Result<String> = runCatching {
        withContext(Dispatchers.Default) {
            val dto = BackupFileDto(
                version = BACKUP_FORMAT_VERSION,
                exportedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
                rooms = gateway.loadAll().toRoomDtos(),
            )
            json.encodeToString(dto)
        }
    }

    override suspend fun parse(rawJson: String): Result<Backup> = runCatching {
        withContext(Dispatchers.Default) {
            val dto = json.decodeFromString<BackupFileDto>(rawJson)
            if (dto.version != BACKUP_FORMAT_VERSION) {
                throw UnsupportedBackupVersionException(dto.version)
            }
            dto.rooms.toDomain()
        }
    }

    override suspend fun restore(backup: Backup): Result<Unit> = runCatching {
        gateway.replaceAll(backup)
    }
}
