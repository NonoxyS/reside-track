package dev.nonoxy.residetrack.core.notifications.domain

import dev.nonoxy.residetrack.common.coroutines.CoroutineDispatchers
import dev.nonoxy.residetrack.common.utils.coRunCatching
import dev.nonoxy.residetrack.core.database.storage.RoomStorage
import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.TimeZone

/**
 * Реактивный оркестратор пушей о выезде: пересобирает дайджесты на каждое изменение БД и
 * переустанавливает запланированные уведомления через [LocalNotifier].
 *
 * Живёт весь процесс (app-scoped [SupervisorJob]) — восстановление backup «просто работает»,
 * т.к. идёт через ту же реактивную БД.
 */
internal class NotificationScheduler(
    private val roomStorage: RoomStorage,
    private val localNotifier: LocalNotifier,
    private val dispatchers: CoroutineDispatchers,
) {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.default)

    fun start() {
        roomStorage.observeAllRoomsWithStudents()
            .map { rooms -> DigestBuilder.build(rooms, Clock.System.now(), TimeZone.currentSystemDefault()) }
            .distinctUntilChanged()
            .onEach { digests -> sync(digests) }
            .catch { throwable -> Napier.e(throwable) { "Error occur on observing checkouts for notifications" } }
            .launchIn(scope)
    }

    private suspend fun sync(digests: List<CheckoutDigest>) {
        coRunCatching(
            tryBlock = { localNotifier.sync(digests) },
            catchBlock = { throwable -> Napier.e(throwable) { "Error occur on syncing checkout notifications" } },
        )
    }
}
