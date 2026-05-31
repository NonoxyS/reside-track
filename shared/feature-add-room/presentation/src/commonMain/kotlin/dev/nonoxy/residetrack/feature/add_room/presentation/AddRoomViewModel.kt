package dev.nonoxy.residetrack.feature.add_room.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore.Intent
import dev.nonoxy.residetrack.feature.add_room.presentation.mappers.UiAddRoomLabelMapper
import dev.nonoxy.residetrack.feature.add_room.presentation.mappers.UiAddRoomStateMapper
import dev.nonoxy.residetrack.feature.add_room.presentation.models.UiAddRoomLabel
import dev.nonoxy.residetrack.feature.add_room.presentation.models.UiAddRoomState
import dev.nonoxy.residetrack.core.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.mapNotNull

class AddRoomViewModel internal constructor(
    private val store: AddRoomStore,
    private val stateMapper: UiAddRoomStateMapper,
    private val labelMapper: UiAddRoomLabelMapper,
) : BaseViewModel<UiAddRoomState, UiAddRoomLabel>(initialState = UiAddRoomState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onFloorNumberInputValueChange(value: String) =
        store.accept(Intent.OnFloorNumberInputValueChange(floorNumber = value))

    fun onFloorNumberSelect(floor: Int) =
        store.accept(Intent.OnFloorNumberSelect(floorNumber = floor))

    fun onRoomNumberInputValueChange(value: String) =
        store.accept(Intent.OnRoomNumberInputValueChange(roomNumber = value))

    fun onBedsCountInputValueChange(value: String) =
        store.accept(Intent.OnBedsCountInputValueChange(bedsCount = value))

    fun onBedsCountSelect(bedsCount: Int) =
        store.accept(Intent.OnBedsCountSelect(bedsCount = bedsCount))

    fun onCreateRoomClick() = store.accept(Intent.OnCreateRoomClick)

    fun onCancelClick() = store.accept(Intent.OnCancelClick)

    fun onToggleFloorInput() = store.accept(Intent.OnToggleFloorInput)

    fun onToggleBedsInput() = store.accept(Intent.OnToggleBedsInput)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
