package dev.nonoxy.feature.add_room.impl.domain

import dev.nonoxy.common.utils.isDigitsOnly
import dev.nonoxy.feature.add_room.api.store.AddRoomErrorKind
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.Intent
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.Label
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.State
import dev.nonoxy.feature.add_room.api.store.AddRoomSuccessKind
import dev.nonoxy.feature.add_room.impl.domain.AddRoomStoreFactory.Action
import dev.nonoxy.feature.add_room.impl.domain.AddRoomStoreFactory.AddRoomErrorKindOrNull
import dev.nonoxy.feature.add_room.impl.domain.AddRoomStoreFactory.Message
import dev.nonoxy.feature.rooms.api.models.Room
import dev.nonoxy.feature.rooms.api.repository.RoomsRepository
import dev.nonoxy.residetrack.core.mvikotlin.BaseExecutor
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher

internal class AddRoomExecutor(
    mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
) : BaseExecutor<Intent, Action, State, Message, Label>(mainContext = mainDispatcher) {

    override suspend fun suspendExecuteAction(action: Action) {
        when (action) {
            Action.LoadExistingRooms -> loadExistingRoomsData()
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.OnFloorNumberInputValueChange -> handleFloorNumberInput(intent.floorNumber)
            is Intent.OnFloorNumberSelect -> handleFloorNumberSelect(intent.floorNumber)
            is Intent.OnRoomNumberInputValueChange -> handleRoomNumberInput(intent.roomNumber)
            is Intent.OnBedsCountInputValueChange -> handleBedsCountInput(intent.bedsCount)
            is Intent.OnBedsCountSelect -> handleBedsCountSelect(intent.bedsCount)
            Intent.OnCreateRoomClick -> handleCreateRoom()
            Intent.OnCancelClick -> publish(Label.CloseScreen)
            Intent.OnToggleFloorInput -> dispatch(Message.ToggleFloorInput)
            Intent.OnToggleBedsInput -> dispatch(Message.ToggleBedsInput)
        }
    }

    private suspend fun loadExistingRoomsData() {
        val existingRooms = roomsRepository.getAllRooms().getOrElse { emptyList() }
        if (existingRooms.isEmpty()) return

        val existingFloors = existingRooms
            .map { it.floorNumber }
            .distinct()
            .sorted()
            .toPersistentList()

        val existingBedsCounts = existingRooms
            .map { it.bedsCount }
            .distinct()
            .sorted()
            .toPersistentList()

        dispatch(
            Message.SetExistingRoomsData(
                existingFloors = existingFloors,
                existingBedsCounts = existingBedsCounts,
            )
        )
    }

    private fun handleFloorNumberInput(value: String) {
        if (value.length > MAX_INPUT_LENGTH) return
        if (!value.isDigitsOnly()) return
        dispatch(Message.SetFloorNumberInput(value = value))
        revalidateForm()
    }

    private fun handleFloorNumberSelect(floor: Int) {
        dispatch(Message.SetFloorNumberSelected(floor = floor))
        revalidateForm()
    }

    private fun handleRoomNumberInput(value: String) {
        if (value.length > MAX_INPUT_LENGTH) return
        if (!value.isDigitsOnly()) return
        dispatch(Message.SetRoomNumberInput(value = value))
        revalidateForm()
    }

    private fun handleBedsCountInput(value: String) {
        if (value.length > MAX_INPUT_LENGTH) return
        if (!value.isDigitsOnly()) return
        dispatch(Message.SetBedsCountInput(value = value))
        revalidateForm()
    }

    private fun handleBedsCountSelect(bedsCount: Int) {
        dispatch(Message.SetBedsCountSelected(bedsCount = bedsCount))
        revalidateForm()
    }

    /** Inline form revalidation on every input — same as old AddRoomViewModel.validateFormOnInput().
     *  Only updates the isFormValid flag (does not surface errors in textfields). */
    private fun revalidateForm() {
        val current = state()
        val isValid = validateFloorNumber(current.floorSelection.textField.value) == null &&
            validateRoomNumber(current.roomNumber.value) == null &&
            validateBedsCount(current.bedsSelection.textField.value) == null
        dispatch(Message.SetIsFormValid(isValid = isValid))
    }

    private suspend fun handleCreateRoom() {
        dispatch(Message.SetIsLoading(isLoading = true))

        val current = state()
        val floorError = validateFloorNumber(current.floorSelection.textField.value)
        val roomError = validateRoomNumber(current.roomNumber.value)
        val bedsError = validateBedsCount(current.bedsSelection.textField.value)

        dispatch(
            Message.SetValidationErrors(
                floorNumberError = AddRoomErrorKindOrNull(floorError),
                roomNumberError = AddRoomErrorKindOrNull(roomError),
                bedsCountError = AddRoomErrorKindOrNull(bedsError),
            )
        )

        if (floorError != null || roomError != null || bedsError != null) {
            dispatch(Message.SetIsFormValid(isValid = false))
            dispatch(Message.SetIsLoading(isLoading = false))
            return
        }
        dispatch(Message.SetIsFormValid(isValid = true))

        val floorNumber = current.floorSelection.textField.value.toInt()
        val roomNumber = current.roomNumber.value.toInt()
        val bedsCount = current.bedsSelection.textField.value.toInt()

        val existingRooms = roomsRepository.getAllRooms().getOrElse { emptyList() }
        val duplicateExists = existingRooms.any { room ->
            room.floorNumber == floorNumber && room.roomNumber == roomNumber
        }
        if (duplicateExists) {
            dispatch(Message.SetIsLoading(isLoading = false))
            publish(
                Label.ShowError(
                    kind = AddRoomErrorKind.RoomAlreadyExists(
                        roomNumber = roomNumber,
                        floorNumber = floorNumber,
                    )
                )
            )
            return
        }

        val newRoom = Room(
            floorNumber = floorNumber,
            roomNumber = roomNumber,
            bedsCount = bedsCount,
            students = emptyList(),
        )

        roomsRepository.saveRoom(newRoom).fold(
            onSuccess = {
                roomsRepository.saveDraftRoom(newRoom).fold(
                    onSuccess = {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(Label.NavigateToManageStudentsDraftRoom)
                    },
                    onFailure = {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(
                            Label.ShowSuccess(
                                kind = AddRoomSuccessKind.RoomCreated(roomNumber = roomNumber)
                            )
                        )
                    },
                )
            },
            onFailure = {
                dispatch(Message.SetIsLoading(isLoading = false))
                publish(Label.ShowError(kind = AddRoomErrorKind.SaveFailed))
            },
        )
    }

    private fun validateFloorNumber(value: String): AddRoomErrorKind? =
        if (value.isBlank()) AddRoomErrorKind.FloorNumberRequired else null

    private fun validateRoomNumber(value: String): AddRoomErrorKind? =
        if (value.isBlank()) AddRoomErrorKind.RoomNumberRequired else null

    private fun validateBedsCount(value: String): AddRoomErrorKind? =
        if (value.isBlank()) AddRoomErrorKind.BedsCountRequired else null
}

private const val MAX_INPUT_LENGTH = 4
