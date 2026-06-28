package dev.nonoxy.residetrack.feature.rooms.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.nonoxy.residetrack.core.backup.domain.model.Backup
import dev.nonoxy.residetrack.core.backup.domain.repository.BackupRepository
import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import dev.nonoxy.residetrack.core.rooms.domain.repository.RoomsRepository
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.Label
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore.State
import kotlinx.coroutines.CoroutineDispatcher

internal class RoomsStoreFactory(
    private val storeFactory: StoreFactory,
    private val mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
    private val backupRepository: BackupRepository,
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
                        backupRepository = backupRepository,
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
        data class SetImportConfirmation(val backup: Backup?) : Message
    }
}
