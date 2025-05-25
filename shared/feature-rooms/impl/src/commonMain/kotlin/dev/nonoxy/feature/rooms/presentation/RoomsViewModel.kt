package dev.nonoxy.feature.rooms.presentation

import dev.nonoxy.common.presentation.BaseViewModel
import dev.nonoxy.feature.rooms.presentation.models.RoomsAction
import dev.nonoxy.feature.rooms.presentation.models.RoomsEvent
import dev.nonoxy.feature.rooms.presentation.models.RoomsViewState
import dev.nonoxy.feature.rooms.repository.RoomsRepository

internal class RoomsViewModel(
    private val roomsRepository: RoomsRepository,
) : BaseViewModel<RoomsViewState, RoomsEvent, RoomsAction>(RoomsViewState.Initial) {

    override fun obtainEvent(event: RoomsEvent) {
        TODO("Not yet implemented")
    }
}
