package dev.nonoxy.residetrack.feature.room_editor.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.nonoxy.residetrack.feature.room_editor.api.models.RoomEditorMode
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorErrorKind
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.Intent
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.Label
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.State
import dev.nonoxy.residetrack.core.rooms.models.Room
import dev.nonoxy.residetrack.core.rooms.repository.RoomsRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineDispatcher

internal class RoomEditorStoreFactory(
    private val storeFactory: StoreFactory,
    private val mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
) {

    fun create(mode: RoomEditorMode): RoomEditorStore =
        object :
            RoomEditorStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = RoomEditorStore::class.simpleName,
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Action.LoadInitial),
                executorFactory = {
                    RoomEditorExecutor(
                        mainDispatcher = mainDispatcher,
                        roomsRepository = roomsRepository,
                        mode = mode,
                    )
                },
                reducer = RoomEditorReducer(),
            ) {}

    internal sealed interface Action {
        data object LoadInitial : Action
    }

    internal sealed interface Message {
        data class SetIsLoading(val isLoading: Boolean) : Message
        data class SetError(val kind: RoomEditorErrorKind) : Message
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

        data class SetShowRoomParams(val show: Boolean) : Message
        data class SetRoomParams(val params: State.RoomParams?) : Message
        data class SetShowDeleteConfirm(val show: Boolean) : Message
        data class SetRoom(val room: Room) : Message
        data class SetRemovingStudentId(val studentId: String?) : Message
    }
}
