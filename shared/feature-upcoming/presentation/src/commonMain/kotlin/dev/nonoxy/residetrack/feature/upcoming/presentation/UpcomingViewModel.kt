package dev.nonoxy.residetrack.feature.upcoming.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import dev.nonoxy.residetrack.core.presentation.viewmodel.BaseViewModel
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore.Intent
import dev.nonoxy.residetrack.feature.upcoming.presentation.mappers.UiUpcomingLabelMapper
import dev.nonoxy.residetrack.feature.upcoming.presentation.mappers.UiUpcomingStateMapper
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingLabel
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingState
import kotlinx.coroutines.flow.mapNotNull

class UpcomingViewModel internal constructor(
    private val store: UpcomingStore,
    private val stateMapper: UiUpcomingStateMapper,
    private val labelMapper: UiUpcomingLabelMapper,
) : BaseViewModel<UiUpcomingState, UiUpcomingLabel>(initialState = UiUpcomingState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onStudentClick(roomId: Long) = store.accept(Intent.OnStudentClick(roomId = roomId))

    fun onRetryClick() = store.accept(Intent.OnRetry)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
