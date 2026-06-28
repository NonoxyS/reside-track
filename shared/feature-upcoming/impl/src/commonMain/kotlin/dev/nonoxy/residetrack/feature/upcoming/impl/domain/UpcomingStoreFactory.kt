package dev.nonoxy.residetrack.feature.upcoming.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.nonoxy.residetrack.core.rooms.domain.repository.RoomsRepository
import dev.nonoxy.residetrack.feature.upcoming.api.models.UpcomingItem
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore.Intent
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore.Label
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore.State
import kotlinx.coroutines.CoroutineDispatcher

internal class UpcomingStoreFactory(
    private val storeFactory: StoreFactory,
    private val mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
) {

    fun create(): UpcomingStore =
        object :
            UpcomingStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = UpcomingStore::class.simpleName,
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Action.LoadInitial),
                executorFactory = {
                    UpcomingExecutor(
                        mainDispatcher = mainDispatcher,
                        roomsRepository = roomsRepository,
                    )
                },
                reducer = UpcomingReducer()
            ) {}

    internal sealed interface Action {
        data object LoadInitial : Action
    }

    internal sealed interface Message {
        data class SetIsLoading(val isLoading: Boolean) : Message
        data object SetError : Message
        data class SetItems(val items: List<UpcomingItem>) : Message
    }
}
