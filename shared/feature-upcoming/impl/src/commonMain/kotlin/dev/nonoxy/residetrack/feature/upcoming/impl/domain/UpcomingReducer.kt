package dev.nonoxy.residetrack.feature.upcoming.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore.State
import dev.nonoxy.residetrack.feature.upcoming.impl.domain.UpcomingStoreFactory.Message

internal class UpcomingReducer : Reducer<State, Message> {

    override fun State.reduce(msg: Message): State = when (msg) {
        is Message.SetIsLoading -> copy(
            isLoading = msg.isLoading,
            isError = if (msg.isLoading) false else isError,
        )
        Message.SetError -> copy(isLoading = false, isError = true)
        is Message.SetItems -> copy(isLoading = false, isError = false, items = msg.items)
    }
}
