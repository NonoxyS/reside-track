package dev.nonoxy.residetrack.feature.add_room.impl.data

import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import dev.nonoxy.residetrack.core.rooms.domain.repository.RoomsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Hand-rolled fake (no mocking library, same approach as the d2-build-helper fakes).
 * Records the calls under test and lets each test pin individual [Result] outcomes.
 */
internal class FakeRoomsRepository(
    rooms: List<Room> = emptyList(),
    draftRoom: Room? = null,
    private val getAllRoomsResult: Result<List<Room>>? = null,
    private val getRoomByIdResult: Result<Room?>? = null,
    private val getDraftRoomResult: Result<Room?>? = null,
    private val saveRoomResult: Result<Long> = Result.success(DEFAULT_SAVED_ID),
    private val saveDraftRoomResult: Result<Unit> = Result.success(Unit),
    private val updateRoomMetadataResult: Result<Unit> = Result.success(Unit),
    private val deleteRoomResult: Result<Unit> = Result.success(Unit),
) : RoomsRepository {

    private val roomsFlow = MutableStateFlow(rooms)
    private val draftFlow = MutableStateFlow(draftRoom)

    /** When set, [observeRooms] emits this error instead of the rooms stream. */
    var observeRoomsError: Throwable? = null

    var savedRoom: Room? = null
        private set
    var saveRoomCallCount: Int = 0
        private set
    var savedDraftRoom: Room? = null
        private set
    var clearDraftCallCount: Int = 0
        private set
    var deletedRoomId: Long? = null
        private set
    var updatedMetadata: Metadata? = null
        private set

    override fun observeRooms(): Flow<List<Room>> =
        observeRoomsError?.let { error -> flow { throw error } } ?: roomsFlow

    override suspend fun getAllRooms(): Result<List<Room>> =
        getAllRoomsResult ?: Result.success(roomsFlow.value)

    override suspend fun getRoomByNumber(roomNumber: Int): Result<Room?> =
        Result.success(roomsFlow.value.firstOrNull { it.roomNumber == roomNumber })

    override suspend fun getRoomById(roomId: Long): Result<Room?> =
        getRoomByIdResult ?: Result.success(roomsFlow.value.firstOrNull { it.id == roomId })

    override suspend fun saveRoom(room: Room): Result<Long> {
        saveRoomCallCount++
        savedRoom = room
        return saveRoomResult
    }

    override suspend fun updateRoomMetadata(
        roomId: Long,
        floorNumber: Int,
        roomNumber: Int,
        bedsCount: Int,
    ): Result<Unit> {
        updatedMetadata = Metadata(roomId, floorNumber, roomNumber, bedsCount)
        return updateRoomMetadataResult
    }

    override suspend fun deleteRoom(roomId: Long): Result<Unit> {
        deletedRoomId = roomId
        return deleteRoomResult
    }

    override suspend fun saveDraftRoom(room: Room): Result<Unit> {
        savedDraftRoom = room
        return saveDraftRoomResult
    }

    override suspend fun getDraftRoom(): Result<Room?> =
        getDraftRoomResult ?: Result.success(draftFlow.value)

    override suspend fun clearDraftRoom(): Result<Unit> {
        clearDraftCallCount++
        draftFlow.value = null
        return Result.success(Unit)
    }

    override fun observeDraftRoom(): Flow<Room?> = draftFlow

    data class Metadata(
        val roomId: Long,
        val floorNumber: Int,
        val roomNumber: Int,
        val bedsCount: Int,
    )

    private companion object {
        const val DEFAULT_SAVED_ID = 1L
    }
}
