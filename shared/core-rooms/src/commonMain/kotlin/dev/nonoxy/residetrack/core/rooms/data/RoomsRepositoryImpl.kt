package dev.nonoxy.residetrack.core.rooms.data

import dev.nonoxy.residetrack.common.coroutines.CoroutineDispatchers
import dev.nonoxy.residetrack.common.utils.coRunCatching
import dev.nonoxy.residetrack.common.utils.wrapFailure
import dev.nonoxy.residetrack.common.utils.wrapSuccess
import dev.nonoxy.residetrack.core.database.dao.RoomDao
import dev.nonoxy.residetrack.core.database.entities.RoomEntity
import dev.nonoxy.residetrack.core.database.relations.RoomWithStudents
import dev.nonoxy.residetrack.core.rooms.data.mappers.RoomMapper
import dev.nonoxy.residetrack.core.rooms.data.mappers.StudentMapper
import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import dev.nonoxy.residetrack.core.rooms.domain.repository.RoomsRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class RoomsRepositoryImpl(
    private val roomDao: RoomDao,
    private val roomMapper: RoomMapper,
    private val studentMapper: StudentMapper,
    private val dispatchers: CoroutineDispatchers,
) : RoomsRepository {

    private val _draftRoom = MutableStateFlow<Room?>(null)

    override fun observeRooms(): Flow<List<Room>> =
        roomDao.observeAllRoomsWithStudents()
            .map { rows -> rows.mapToDomain() }
            .flowOn(dispatchers.io)

    override suspend fun getAllRooms(): Result<List<Room>> = withContext(dispatchers.io) {
        coRunCatching(
            tryBlock = {
                roomDao.getAllRoomsWithStudents()
                    .mapToDomain()
                    .wrapSuccess()
            },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on getting all rooms" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun getRoomByNumber(
        roomNumber: Int
    ): Result<Room?> = withContext(dispatchers.io) {
        coRunCatching(
            tryBlock = {
                roomDao.getRoomWithStudentsByRoomNumber(roomNumber = roomNumber)
                    ?.mapToDomain()
                    .wrapSuccess()
            },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on getting room by number: $roomNumber" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun getRoomById(
        roomId: Long
    ): Result<Room?> = withContext(dispatchers.io) {
        coRunCatching(
            tryBlock = {
                roomDao.getRoomWithStudentsById(roomId = roomId)
                    ?.mapToDomain()
                    .wrapSuccess()
            },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on getting room by id: $roomId" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun saveRoom(room: Room): Result<Long> = withContext(dispatchers.io) {
        coRunCatching(
            tryBlock = {
                val roomEntity = room.mapToEntity()
                val studentEntities = studentMapper.map(items = room.students, roomId = room.id)
                val persistedId = roomDao.saveRoomWithStudents(
                    room = roomEntity,
                    students = studentEntities,
                )
                persistedId.wrapSuccess()
            },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on saving room: $room" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun updateRoomMetadata(
        roomId: Long,
        floorNumber: Int,
        roomNumber: Int,
        bedsCount: Int,
    ): Result<Unit> = withContext(dispatchers.io) {
        coRunCatching(
            tryBlock = {
                roomDao.updateRoomMetadata(
                    roomId = roomId,
                    floorNumber = floorNumber,
                    roomNumber = roomNumber,
                    bedsCount = bedsCount,
                )
                Unit.wrapSuccess()
            },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error updating room metadata: $roomId" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun deleteRoom(roomId: Long): Result<Unit> = withContext(dispatchers.io) {
        coRunCatching(
            tryBlock = {
                roomDao.deleteRoomWithStudents(roomId = roomId)
                Unit.wrapSuccess()
            },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error deleting room: $roomId" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun saveDraftRoom(room: Room): Result<Unit> = withContext(dispatchers.io) {
        coRunCatching(
            tryBlock = {
                _draftRoom.value = room
                Unit.wrapSuccess()
            },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on saving draft room: $room" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun getDraftRoom(): Result<Room?> = withContext(dispatchers.io) {
        coRunCatching(
            tryBlock = { _draftRoom.value.wrapSuccess() },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on getting draft room" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun clearDraftRoom(): Result<Unit> = withContext(dispatchers.io) {
        coRunCatching(
            tryBlock = {
                _draftRoom.value = null
                Unit.wrapSuccess()
            },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on clearing draft room" }
                throwable.wrapFailure()
            }
        )
    }

    override fun observeDraftRoom(): Flow<Room?> = _draftRoom.asStateFlow()

    private fun RoomWithStudents.mapToDomain(): Room = roomMapper.map(this)
    private fun List<RoomWithStudents>.mapToDomain(): List<Room> = map { it.mapToDomain() }
    private fun Room.mapToEntity(): RoomEntity = roomMapper.map(this)
}
