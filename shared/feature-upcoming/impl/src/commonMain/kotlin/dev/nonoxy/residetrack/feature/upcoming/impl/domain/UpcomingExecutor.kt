package dev.nonoxy.residetrack.feature.upcoming.impl.domain

import dev.nonoxy.residetrack.common.utils.currentLocalDate
import dev.nonoxy.residetrack.core.mvikotlin.BaseExecutor
import dev.nonoxy.residetrack.core.rooms.domain.repository.RoomsRepository
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore.Intent
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore.Label
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore.State
import dev.nonoxy.residetrack.feature.upcoming.impl.domain.UpcomingStoreFactory.Action
import dev.nonoxy.residetrack.feature.upcoming.impl.domain.UpcomingStoreFactory.Message
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch

internal class UpcomingExecutor(
    mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
) : BaseExecutor<Intent, Action, State, Message, Label>(mainContext = mainDispatcher) {

    override suspend fun suspendExecuteAction(action: Action) {
        when (action) {
            Action.LoadInitial -> observeUpcoming()
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.OnStudentClick -> publish(
                Label.NavigateToRoomEditorExistingRoom(roomId = intent.roomId.toString())
            )
            Intent.OnRetry -> observeUpcoming()
        }
    }

    private suspend fun observeUpcoming() {
        dispatch(Message.SetIsLoading(isLoading = true))
        roomsRepository.observeRooms()
            .catch { dispatch(Message.SetError) }
            .collect { rooms ->
                dispatch(Message.SetItems(items = UpcomingMapper.map(rooms = rooms, today = currentLocalDate)))
            }
    }
}
