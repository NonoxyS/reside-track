package dev.nonoxy.residetrack.feature.add_room.impl.data

import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import dev.nonoxy.residetrack.core.rooms.domain.repository.DraftRoomRepository

/** Records the draft handoff so tests can assert what add-room saved. */
internal class FakeDraftRoomRepository(
    draftRoom: Room? = null,
    private val saveResult: Result<Unit> = Result.success(Unit),
) : DraftRoomRepository {

    private var draft: Room? = draftRoom

    var savedDraftRoom: Room? = null
        private set
    var clearCallCount: Int = 0
        private set

    override suspend fun save(room: Room): Result<Unit> {
        savedDraftRoom = room
        draft = room
        return saveResult
    }

    override suspend fun get(): Result<Room?> = Result.success(draft)

    override suspend fun clear(): Result<Unit> {
        clearCallCount++
        draft = null
        return Result.success(Unit)
    }
}
