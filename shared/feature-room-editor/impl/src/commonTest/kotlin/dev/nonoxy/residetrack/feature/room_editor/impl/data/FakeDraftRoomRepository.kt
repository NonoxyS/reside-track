package dev.nonoxy.residetrack.feature.room_editor.impl.data

import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import dev.nonoxy.residetrack.core.rooms.domain.repository.DraftRoomRepository

/** Serves a preset draft and records the clear so tests can assert the handoff. */
internal class FakeDraftRoomRepository(
    draftRoom: Room? = null,
    private val getResult: Result<Room?>? = null,
) : DraftRoomRepository {

    private var draft: Room? = draftRoom

    var savedDraftRoom: Room? = null
        private set
    var clearCallCount: Int = 0
        private set

    override suspend fun save(room: Room): Result<Unit> {
        savedDraftRoom = room
        draft = room
        return Result.success(Unit)
    }

    override suspend fun get(): Result<Room?> = getResult ?: Result.success(draft)

    override suspend fun clear(): Result<Unit> {
        clearCallCount++
        draft = null
        return Result.success(Unit)
    }
}
