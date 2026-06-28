package dev.nonoxy.residetrack.feature.rooms.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.residetrack.feature.rooms.presentation.mappers.UiRoomsLabelMapper
import dev.nonoxy.residetrack.feature.rooms.presentation.mappers.UiRoomsStateMapper
import dev.nonoxy.residetrack.feature.rooms.presentation.models.UiRoomsLabel
import dev.nonoxy.residetrack.feature.rooms.presentation.models.UiRoomsState
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

    fun onRetryClick() = store.accept(Intent.OnRetry)

    fun onExportClick() = store.accept(Intent.OnExportClick)

    fun onExportCompleted(success: Boolean) = store.accept(Intent.OnExportCompleted(success = success))

    fun onImportClick() = store.accept(Intent.OnImportClick)

    fun onBackupFileLoaded(json: String) = store.accept(Intent.OnBackupFileLoaded(json = json))

    fun onRestoreConfirm() = store.accept(Intent.OnRestoreConfirm)

    fun onRestoreCancel() = store.accept(Intent.OnRestoreCancel)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
