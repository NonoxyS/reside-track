package dev.nonoxy.residetrack.core.rooms.data.storage

import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import kotlinx.coroutines.flow.MutableStateFlow

/** [DraftRoomStorage] backed by an in-memory [MutableStateFlow]. */
internal class InMemoryDraftRoomStorage : DraftRoomStorage {

    private val draft = MutableStateFlow<Room?>(null)

    override fun save(room: Room) {
        draft.value = room
    }

    override fun get(): Room? = draft.value

    override fun clear() {
        draft.value = null
    }
}
