package dev.nonoxy.residetrack.feature.rooms.impl.domain

import dev.nonoxy.residetrack.core.rooms.repository.RoomsRepository
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Label
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.State
import dev.nonoxy.residetrack.feature.rooms.impl.domain.RoomsStoreFactory.Action
import dev.nonoxy.residetrack.feature.rooms.impl.domain.RoomsStoreFactory.Message
import dev.nonoxy.residetrack.core.mvikotlin.BaseExecutor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch

internal class RoomsExecutor(
    mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
) : BaseExecutor<Intent, Action, State, Message, Label>(mainContext = mainDispatcher) {

    override suspend fun suspendExecuteAction(action: Action) {
        when (action) {
            Action.LoadInitial -> loadRoomsData()
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.OnRoomClick -> publish(
                Label.NavigateToRoomEditorExistingRoom(roomId = intent.roomId.toString())
            )
            Intent.OnAddRoomClick -> publish(Label.NavigateToAddRoomScreen)
            Intent.OnRetry -> loadRoomsData()
        }
    }

    private suspend fun loadRoomsData() {
        dispatch(Message.SetIsLoading(isLoading = true))
        roomsRepository.observeRooms()
            .catch { dispatch(Message.SetError) }
            .collect { rooms ->
                val grouped = rooms.groupBy { room -> room.floorNumber }
                dispatch(Message.SetRoomsOnFloor(roomsOnFloor = grouped))
            }
    }
}
