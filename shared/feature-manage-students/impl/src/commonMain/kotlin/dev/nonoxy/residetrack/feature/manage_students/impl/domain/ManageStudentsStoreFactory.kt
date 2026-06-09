package dev.nonoxy.residetrack.feature.manage_students.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.nonoxy.residetrack.feature.manage_students.api.models.ManageStudentsMode
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsErrorKind
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore.Intent
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore.Label
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore.State
import dev.nonoxy.residetrack.core.rooms.models.Room
import dev.nonoxy.residetrack.core.rooms.repository.RoomsRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineDispatcher

internal class ManageStudentsStoreFactory(
    private val storeFactory: StoreFactory,
    private val mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
) {

    fun create(mode: ManageStudentsMode): ManageStudentsStore =
        object :
            ManageStudentsStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = ManageStudentsStore::class.simpleName,
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Action.LoadInitial),
                executorFactory = {
                    ManageStudentsExecutor(
                        mainDispatcher = mainDispatcher,
                        roomsRepository = roomsRepository,
                        mode = mode,
                    )
                },
                reducer = ManageStudentsReducer(),
            ) {}

    internal sealed interface Action {
        data object LoadInitial : Action
    }

    internal sealed interface Message {
        data class SetIsLoading(val isLoading: Boolean) : Message
        data class SetError(val kind: ManageStudentsErrorKind) : Message
        data object ClearError : Message
        data class SetRoomAndStudents(
            val room: Room,
            val editableStudents: ImmutableList<State.EditableStudent>,
        ) : Message

        data class SetEditableStudents(
            val editableStudents: ImmutableList<State.EditableStudent>,
        ) : Message

        data class SetShowDiscardConfirm(val show: Boolean) : Message

        data class SetOpenDatePicker(val openPicker: State.OpenPicker?) : Message
    }
}
