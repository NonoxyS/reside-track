package dev.nonoxy.feature.rooms.presentation

import androidx.lifecycle.viewModelScope
import dev.nonoxy.common.presentation.BaseViewModel
import dev.nonoxy.feature.rooms.presentation.models.RoomsAction
import dev.nonoxy.feature.rooms.presentation.models.RoomsEvent
import dev.nonoxy.feature.rooms.presentation.models.RoomsViewState
import dev.nonoxy.feature.rooms.api.repository.RoomsRepository
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomMapper
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.launch

// TODO Task 7: this class is replaced by the new RoomsViewModel in :shared:feature-rooms:presentation.
//  Kept renamed (Old prefix) only to avoid FQN collision during the Task 5→7 transition.
internal class OldRoomsViewModel(
    private val roomsRepository: RoomsRepository,
    private val roomMapper: UiRoomMapper
) : BaseViewModel<RoomsViewState, RoomsEvent, RoomsAction>(RoomsViewState.Initial) {

    override fun obtainEvent(event: RoomsEvent) {
        when (event) {
            is RoomsEvent.OnRoomClick -> {
                viewAction = RoomsAction.NavigateToManageStudentsExistingRoom(event.roomId.toString())
            }
            is RoomsEvent.OnTabSelect -> TODO()
            RoomsEvent.OnAddRoomClick -> viewAction = RoomsAction.NavigateToAddRoomScreen
        }
    }

    init {
        viewModelScope.launch {
            loadRoomsData()
        }
    }

    private suspend fun loadRoomsData() {
        val rooms = roomsRepository.getAllRooms().getOrElse { emptyList() }

        if (rooms.isNotEmpty()) {
            val roomsGroupedFloor = rooms
                .groupBy { room -> room.floorNumber }
                .mapValues { room -> room.value.let(roomMapper::map).toPersistentList() }
                .toPersistentMap()

            viewState = viewState.copy(roomsOnFloor = roomsGroupedFloor)
        }
    }
}
