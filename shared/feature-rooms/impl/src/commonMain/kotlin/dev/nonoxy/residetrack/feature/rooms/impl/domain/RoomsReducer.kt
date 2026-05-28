package dev.nonoxy.residetrack.feature.rooms.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.State
import dev.nonoxy.residetrack.feature.rooms.impl.domain.RoomsStoreFactory.Message

internal class RoomsReducer : Reducer<State, Message> {

    override fun State.reduce(msg: Message): State = when (msg) {
        is Message.SetRoomsOnFloor -> copy(roomsOnFloor = msg.roomsOnFloor)
    }
}
