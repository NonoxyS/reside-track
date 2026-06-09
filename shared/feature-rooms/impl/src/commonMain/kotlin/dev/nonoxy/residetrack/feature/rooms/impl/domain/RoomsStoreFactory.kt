package dev.nonoxy.residetrack.feature.rooms.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.nonoxy.residetrack.core.rooms.models.Room
import dev.nonoxy.residetrack.core.rooms.repository.RoomsRepository
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Label
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.State
import kotlinx.coroutines.CoroutineDispatcher

internal class RoomsStoreFactory(
    private val storeFactory: StoreFactory,
    private val mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
) {

    fun create(): RoomsStore =
        object :
            RoomsStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = RoomsStore::class.simpleName,
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Action.LoadInitial),
                executorFactory = {
                    RoomsExecutor(
                        mainDispatcher = mainDispatcher,
                        roomsRepository = roomsRepository,
                    )
                },
                reducer = RoomsReducer()
            ) {}

    internal sealed interface Action {
        data object LoadInitial : Action
    }

    internal sealed interface Message {
        data class SetIsLoading(val isLoading: Boolean) : Message
        data object SetError : Message
        data class SetRoomsOnFloor(val roomsOnFloor: Map<Int, List<Room>>) : Message
    }
}
