package dev.nonoxy.residetrack.feature.upcoming.api.store

import com.arkivanov.mvikotlin.core.store.Store
import dev.nonoxy.residetrack.feature.upcoming.api.models.UpcomingItem
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore.Intent
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore.Label
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore.State

interface UpcomingStore : Store<Intent, State, Label> {

    data class State(
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        val items: List<UpcomingItem> = emptyList(),
    )

    sealed interface Intent {
        data class OnStudentClick(val roomId: Long) : Intent
        data object OnRetry : Intent
    }

    sealed interface Label {
        data class NavigateToManageStudentsExistingRoom(val roomId: String) : Label
    }
}
