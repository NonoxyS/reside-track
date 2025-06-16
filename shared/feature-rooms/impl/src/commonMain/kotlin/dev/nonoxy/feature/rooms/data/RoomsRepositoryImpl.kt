package dev.nonoxy.feature.rooms.data

import dev.nonoxy.common.utils.coRunCatching
import dev.nonoxy.common.utils.wrapFailure
import dev.nonoxy.common.utils.wrapSuccess
import dev.nonoxy.core.database.dao.RoomDao
import dev.nonoxy.core.database.entities.RoomEntity
import dev.nonoxy.core.database.relations.RoomWithStudents
import dev.nonoxy.feature.rooms.data.mappers.RoomMapper
import dev.nonoxy.feature.rooms.models.Room
import dev.nonoxy.feature.rooms.repository.RoomsRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

internal class RoomsRepositoryImpl(
    private val roomDao: RoomDao,
    private val roomMapper: RoomMapper,
    private val ioDispatcher: CoroutineDispatcher = dev.nonoxy.common.coroutines.ioDispatcher
) : RoomsRepository {

    private val _draftRoom = MutableStateFlow<Room?>(null)

    override suspend fun getAllRooms(): Result<List<Room>> = withContext(ioDispatcher) {
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

    override suspend fun getRoomsByFloor(
        floorNumber: Int
    ): Result<List<Room>> = withContext(ioDispatcher) {
        coRunCatching(
            tryBlock = {
                roomDao.getRoomsWithStudentsByFloor(floor = floorNumber)
                    .mapToDomain()
                    .wrapSuccess()
            },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on getting rooms by floor: $floorNumber" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun getRoomByNumber(
        roomNumber: Int
    ): Result<Room?> = withContext(ioDispatcher) {
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
    ): Result<Room?> = withContext(ioDispatcher) {
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

    override suspend fun saveRoom(room: Room): Result<Unit> = withContext(ioDispatcher) {
        coRunCatching(
            tryBlock = { roomDao.insertRoom(room = room.mapToEntity()).wrapSuccess() },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on saving room: $room" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun saveDraftRoom(room: Room): Result<Unit> = withContext(ioDispatcher) {
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

    override suspend fun getDraftRoom(): Result<Room?> = withContext(ioDispatcher) {
        coRunCatching(
            tryBlock = { _draftRoom.value.wrapSuccess() },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on getting draft room" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun clearDraftRoom(): Result<Unit> = withContext(ioDispatcher) {
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

    override fun observeDraftRoom(): StateFlow<Room?> = _draftRoom.asStateFlow()

    private fun RoomWithStudents.mapToDomain(): Room = roomMapper.map(this)
    private fun List<RoomWithStudents>.mapToDomain(): List<Room> = map { it.mapToDomain() }
    private fun Room.mapToEntity(): RoomEntity = roomMapper.map(this)
}
