package dev.nonoxy.feature.rooms.presentation

import androidx.lifecycle.viewModelScope
import dev.nonoxy.common.presentation.BaseViewModel
import dev.nonoxy.feature.rooms.presentation.models.RoomsAction
import dev.nonoxy.feature.rooms.presentation.models.RoomsEvent
import dev.nonoxy.feature.rooms.presentation.models.RoomsViewState
import dev.nonoxy.feature.rooms.repository.RoomsRepository
import dev.nonoxy.feature.rooms.ui.mappers.UiRoomMapper
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.launch

internal class RoomsViewModel(
    private val roomsRepository: RoomsRepository,
    private val roomMapper: UiRoomMapper
) : BaseViewModel<RoomsViewState, RoomsEvent, RoomsAction>(RoomsViewState.Initial) {

    override fun obtainEvent(event: RoomsEvent) {
        when (event) {
            is RoomsEvent.OnRoomClick -> TODO()
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
