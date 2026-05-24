package dev.nonoxy.feature.rooms.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import dev.nonoxy.feature.rooms.api.store.RoomsStore
import dev.nonoxy.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomsLabelMapper
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomsStateMapper
import dev.nonoxy.feature.rooms.presentation.models.UiRoomsLabel
import dev.nonoxy.feature.rooms.presentation.models.UiRoomsState
import dev.nonoxy.residetrack.core.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.mapNotNull

class RoomsViewModel internal constructor(
    private val store: RoomsStore,
    private val stateMapper: UiRoomsStateMapper,
    private val labelMapper: UiRoomsLabelMapper,
) : BaseViewModel<UiRoomsState, UiRoomsLabel>(initialState = UiRoomsState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onRoomClick(roomId: Long) = store.accept(Intent.OnRoomClick(roomId = roomId))

    fun onAddRoomClick() = store.accept(Intent.OnAddRoomClick)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
