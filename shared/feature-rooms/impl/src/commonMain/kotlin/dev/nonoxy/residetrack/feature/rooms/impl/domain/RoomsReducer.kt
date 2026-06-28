package dev.nonoxy.residetrack.feature.rooms.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.State
import dev.nonoxy.residetrack.feature.rooms.impl.domain.RoomsStoreFactory.Message

internal class RoomsReducer : Reducer<State, Message> {

    override fun State.reduce(msg: Message): State = when (msg) {
        is Message.SetIsLoading -> copy(
            isLoading = msg.isLoading,
            isError = if (msg.isLoading) false else isError,
        )

        Message.SetError -> copy(
            isLoading = false,
            isError = true,
        )

        is Message.SetRoomsOnFloor -> copy(
            isLoading = false,
            isError = false,
            roomsOnFloor = msg.roomsOnFloor,
        )

        is Message.SetImportConfirmation -> copy(
            importConfirmation = msg.backup,
        )
    }
}
