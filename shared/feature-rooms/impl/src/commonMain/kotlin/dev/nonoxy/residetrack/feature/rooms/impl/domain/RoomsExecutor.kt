package dev.nonoxy.residetrack.feature.rooms.impl.domain

import dev.nonoxy.residetrack.feature.rooms.api.repository.RoomsRepository
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Label
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.State
import dev.nonoxy.residetrack.feature.rooms.impl.domain.RoomsStoreFactory.Action
import dev.nonoxy.residetrack.feature.rooms.impl.domain.RoomsStoreFactory.Message
import dev.nonoxy.residetrack.core.mvikotlin.BaseExecutor
import kotlinx.coroutines.CoroutineDispatcher

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
                Label.NavigateToManageStudentsExistingRoom(roomId = intent.roomId.toString())
            )
            Intent.OnAddRoomClick -> publish(Label.NavigateToAddRoomScreen)
        }
    }

    private suspend fun loadRoomsData() {
        // TODO: silent error swallow inherited from old ViewModel. When State gains
        //  isError/isLoading fields (post-Phase 4), dispatch a Message.SetError here.
        val rooms = roomsRepository.getAllRooms().getOrElse { emptyList() }
        if (rooms.isNotEmpty()) {
            val grouped = rooms.groupBy { room -> room.floorNumber }
            dispatch(Message.SetRoomsOnFloor(roomsOnFloor = grouped))
        }
    }
}
