package dev.nonoxy.residetrack.core.rooms.data

import dev.nonoxy.residetrack.common.coroutines.CoroutineDispatchers
import dev.nonoxy.residetrack.common.utils.coRunCatching
import dev.nonoxy.residetrack.common.utils.wrapFailure
import dev.nonoxy.residetrack.common.utils.wrapSuccess
import dev.nonoxy.residetrack.core.rooms.data.storage.DraftRoomStorage
import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import dev.nonoxy.residetrack.core.rooms.domain.repository.DraftRoomRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext

internal class DraftRoomRepositoryImpl(
    private val storage: DraftRoomStorage,
    private val dispatchers: CoroutineDispatchers,
) : DraftRoomRepository {

    override suspend fun save(room: Room): Result<Unit> = withContext(dispatchers.io) {
        coRunCatching(
            tryBlock = { storage.save(room).wrapSuccess() },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on saving draft room: $room" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun get(): Result<Room?> = withContext(dispatchers.io) {
        coRunCatching(
            tryBlock = { storage.get().wrapSuccess() },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on getting draft room" }
                throwable.wrapFailure()
            }
        )
    }

    override suspend fun clear(): Result<Unit> = withContext(dispatchers.io) {
        coRunCatching(
            tryBlock = { storage.clear().wrapSuccess() },
            catchBlock = { throwable ->
                Napier.e(throwable) { "Error occur on clearing draft room" }
                throwable.wrapFailure()
            }
        )
    }
}
