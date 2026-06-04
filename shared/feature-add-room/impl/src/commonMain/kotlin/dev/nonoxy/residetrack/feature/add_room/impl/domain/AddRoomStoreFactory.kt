package dev.nonoxy.residetrack.feature.add_room.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore.Intent
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore.Label
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore.State
import dev.nonoxy.residetrack.core.rooms.repository.RoomsRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineDispatcher

internal class AddRoomStoreFactory(
    private val storeFactory: StoreFactory,
    private val mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
) {

    fun create(): AddRoomStore =
        object :
            AddRoomStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = AddRoomStore::class.simpleName,
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Action.LoadExistingRooms),
                executorFactory = {
                    AddRoomExecutor(
                        mainDispatcher = mainDispatcher,
                        roomsRepository = roomsRepository,
                    )
                },
                reducer = AddRoomReducer(),
            ) {}

    internal sealed interface Action {
        data object LoadExistingRooms : Action
    }

    internal sealed interface Message {
        data class SetExistingRoomsData(
            val existingFloors: ImmutableList<Int>,
            val existingBedsCounts: ImmutableList<Int>,
        ) : Message

        data class SetFloorNumberInput(val value: String) : Message
        data class SetFloorNumberSelected(val floor: Int) : Message
        data object ToggleFloorInput : Message

        data class SetRoomNumberInput(val value: String) : Message

        data class SetBedsCountInput(val value: String) : Message
        data class SetBedsCountSelected(val bedsCount: Int) : Message
        data object ToggleBedsInput : Message

        data class SetValidationErrors(
            val floorNumberError: AddRoomErrorKindOrNull,
            val roomNumberError: AddRoomErrorKindOrNull,
            val bedsCountError: AddRoomErrorKindOrNull,
        ) : Message

        data class SetIsFormValid(val isValid: Boolean) : Message
        data class SetIsLoading(val isLoading: Boolean) : Message
        data class SetShowDiscardConfirm(val show: Boolean) : Message
    }

    /** Wrapper used because `Reducer.reduce` can't carry a triple of nullable values nicely
     *  without losing type-safety; also lets us swap to a Map<Field, ErrorKind?> later. */
    internal data class AddRoomErrorKindOrNull(
        val value: dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomErrorKind?
    )
}
