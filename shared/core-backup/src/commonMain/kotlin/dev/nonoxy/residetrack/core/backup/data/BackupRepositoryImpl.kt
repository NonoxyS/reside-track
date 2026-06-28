package dev.nonoxy.residetrack.core.backup.data

import dev.nonoxy.residetrack.core.backup.data.gateway.BackupGateway
import dev.nonoxy.residetrack.core.backup.data.mapper.toDomain
import dev.nonoxy.residetrack.core.backup.data.mapper.toRoomDtos
import dev.nonoxy.residetrack.core.backup.data.model.BACKUP_FORMAT_VERSION
import dev.nonoxy.residetrack.core.backup.data.model.BackupFileDto
import dev.nonoxy.residetrack.core.backup.domain.model.Backup
import dev.nonoxy.residetrack.core.backup.domain.repository.BackupRepository
import kotlinx.serialization.json.Json
import kotlin.time.Clock

internal class BackupRepositoryImpl(
    private val gateway: BackupGateway,
    private val json: Json,
) : BackupRepository {

    override suspend fun export(): Result<String> = runCatching {
        val dto = BackupFileDto(
            version = BACKUP_FORMAT_VERSION,
            exportedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
            rooms = gateway.loadAll().toRoomDtos(),
        )
        json.encodeToString(dto)
    }

    override suspend fun parse(rawJson: String): Result<Backup> = runCatching {
        val dto = json.decodeFromString<BackupFileDto>(rawJson)
        require(dto.version == BACKUP_FORMAT_VERSION) {
            "Unsupported backup version: ${dto.version}"
        }
        dto.rooms.toDomain()
    }

    override suspend fun restore(backup: Backup): Result<Unit> = runCatching {
        gateway.replaceAll(backup)
    }
}
