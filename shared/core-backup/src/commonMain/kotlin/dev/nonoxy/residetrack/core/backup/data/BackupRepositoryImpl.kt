package dev.nonoxy.residetrack.core.backup.data

import dev.nonoxy.residetrack.core.backup.data.mapper.toBackup
import dev.nonoxy.residetrack.core.backup.data.mapper.toDomain
import dev.nonoxy.residetrack.core.backup.data.mapper.toRoomDtos
import dev.nonoxy.residetrack.core.backup.data.mapper.toRoomRows
import dev.nonoxy.residetrack.core.backup.data.mapper.toStudentRowsByRoom
import dev.nonoxy.residetrack.core.backup.data.model.BACKUP_FORMAT_VERSION
import dev.nonoxy.residetrack.core.backup.data.model.BackupFileDto
import dev.nonoxy.residetrack.core.backup.domain.model.Backup
import dev.nonoxy.residetrack.core.backup.domain.model.UnsupportedBackupVersionException
import dev.nonoxy.residetrack.core.backup.domain.repository.BackupRepository
import dev.nonoxy.residetrack.core.database.storage.RoomStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.time.Clock

internal class BackupRepositoryImpl(
    private val roomStorage: RoomStorage,
    private val json: Json,
) : BackupRepository {

    // Serialization is CPU-bound; keep it off the caller's dispatcher. Room confines its own DB threading.
    override suspend fun export(): Result<String> = runCatching {
        withContext(Dispatchers.Default) {
            val dto = BackupFileDto(
                version = BACKUP_FORMAT_VERSION,
                exportedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
                rooms = roomStorage.getAllRoomsWithStudents().toBackup().toRoomDtos(),
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
        roomStorage.replaceAllRoomsWithStudents(
            rooms = backup.toRoomRows(),
            studentsByRoom = backup.toStudentRowsByRoom(),
        )
    }
}
