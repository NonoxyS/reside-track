package dev.nonoxy.residetrack.core.backup.data

import dev.nonoxy.residetrack.common.coroutines.CoroutineDispatchers
import dev.nonoxy.residetrack.common.utils.coRunCatching
import dev.nonoxy.residetrack.common.utils.wrapFailure
import dev.nonoxy.residetrack.common.utils.wrapSuccess
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
import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.time.Clock

internal class BackupRepositoryImpl(
    private val roomStorage: RoomStorage,
    private val json: Json,
    private val dispatchers: CoroutineDispatchers,
) : BackupRepository {

    override suspend fun export(): Result<String> = withContext(dispatchers.default) {
        coRunCatching(
            tryBlock = {
                val dto = BackupFileDto(
                    version = BACKUP_FORMAT_VERSION,
                    exportedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
                    rooms = roomStorage.getAllRoomsWithStudents().toBackup().toRoomDtos(),
                )
                json.encodeToString(dto).wrapSuccess()
            },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on exporting backup" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun parse(rawJson: String): Result<Backup> = withContext(dispatchers.default) {
        coRunCatching(
            tryBlock = {
                val dto = json.decodeFromString<BackupFileDto>(rawJson)
                if (dto.version != BACKUP_FORMAT_VERSION) {
                    throw UnsupportedBackupVersionException(dto.version)
                }
                dto.rooms.toDomain().wrapSuccess()
            },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on parsing backup" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun restore(backup: Backup): Result<Unit> = withContext(dispatchers.io) {
        coRunCatching(
            tryBlock = {
                roomStorage.replaceAllRoomsWithStudents(
                    rooms = backup.toRoomRows(),
                    studentsByRoom = backup.toStudentRowsByRoom(),
                )
                Unit.wrapSuccess()
            },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on restoring backup" }
                throwable.wrapFailure()
            }
        )
    }
}
