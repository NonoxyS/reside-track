package dev.nonoxy.residetrack.feature.rooms.api.store

import com.arkivanov.mvikotlin.core.store.Store
import dev.nonoxy.residetrack.feature.rooms.api.models.Room
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Label
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.State

interface RoomsStore : Store<Intent, State, Label> {

    data class State(
        val roomsOnFloor: Map<Int, List<Room>> = emptyMap(),
    )

    sealed interface Intent {
        data class OnRoomClick(val roomId: Long) : Intent
        data object OnAddRoomClick : Intent
    }

    sealed interface Label {
        data object NavigateToAddRoomScreen : Label
        data class NavigateToManageStudentsExistingRoom(val roomId: String) : Label
    }
}
