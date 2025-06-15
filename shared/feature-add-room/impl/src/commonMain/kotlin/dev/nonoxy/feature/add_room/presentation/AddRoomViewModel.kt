package dev.nonoxy.feature.add_room.presentation

import androidx.compose.ui.util.fastMap
import androidx.lifecycle.viewModelScope
import dev.nonoxy.common.presentation.BaseViewModel
import dev.nonoxy.common.utils.isDigitsOnly
import dev.nonoxy.feature.add_room.presentation.models.AddRoomAction
import dev.nonoxy.feature.add_room.presentation.models.AddRoomEvent
import dev.nonoxy.feature.add_room.presentation.models.AddRoomViewState
import dev.nonoxy.feature.add_room.presentation.models.BedsSelectionState
import dev.nonoxy.feature.add_room.presentation.models.FloorSelectionState
import dev.nonoxy.feature.add_room.presentation.models.TextFieldState
import dev.nonoxy.feature.rooms.models.Room
import dev.nonoxy.feature.rooms.repository.RoomsRepository
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import residetrack.shared.feature_add_room.impl.generated.resources.Res
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_error_room_already_exists
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_error_save_failed
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_error_unknown
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_success_message
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_validation_beds_count_required
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_validation_floor_number_required
import residetrack.shared.feature_add_room.impl.generated.resources.add_room_validation_room_number_required

internal class AddRoomViewModel(
    private val roomsRepository: RoomsRepository
) : BaseViewModel<AddRoomViewState, AddRoomEvent, AddRoomAction>(AddRoomViewState.Initial) {

    override fun obtainEvent(event: AddRoomEvent) {
        when (event) {
            is AddRoomEvent.OnFloorNumberInputValueChange -> handleFloorNumberInputValueChange(
                floorNumber = event.floorNumber
            )

            is AddRoomEvent.OnFloorNumberSelect -> handleFloorNumberSelect(floorNumber = event.floorNumber)
            is AddRoomEvent.OnRoomNumberInputValueChange -> handleRoomNumberInputValueChange(
                roomNumber = event.roomNumber
            )

            is AddRoomEvent.OnBedsCountInputValueChange -> handleBedsCountInputValueChange(bedsCount = event.bedsCount)
            is AddRoomEvent.OnBedsCountSelect -> handleBedsCountSelect(bedsCount = event.bedsCount)
            is AddRoomEvent.OnCreateRoomClick -> handleOnCreateRoomClick()
            is AddRoomEvent.OnCancelClick -> handleOnCancelClick()
            AddRoomEvent.OnToggleFloorInput -> handleToggleFloorInput()
            AddRoomEvent.OnToggleBedsInput -> handleToggleBedsInput()
        }
    }

    init {
        viewModelScope.launch {
            loadExistingRoomsData()
        }
    }

    private suspend fun loadExistingRoomsData() {
        val existingRooms = roomsRepository.getAllRooms().getOrElse { emptyList() }

        if (existingRooms.isNotEmpty()) {
            val existingFloors = existingRooms
                .fastMap { it.floorNumber }
                .distinct()
                .sorted()
                .toPersistentList()

            val existingBedsCounts = existingRooms
                .map { it.bedsCount }
                .distinct()
                .sorted()
                .toPersistentList()

            viewState = viewState.copy(
                floorSelection = viewState.floorSelection.copy(
                    existingFloors = existingFloors
                ),
                bedsSelection = viewState.bedsSelection.copy(
                    existingBedsCounts = existingBedsCounts
                ),
                hasExistingRooms = true
            )
        }
    }

    private fun handleFloorNumberInputValueChange(floorNumber: String) {
        if (!floorNumber.isDigitsOnly()) return

        viewState = viewState.copy(
            floorSelection = viewState.floorSelection.updateTextField(floorNumber)
        )
        validateFormOnInput()
    }

    private fun handleFloorNumberSelect(floorNumber: Int) {
        viewState = viewState.copy(
            floorSelection = viewState.floorSelection.selectFloor(floorNumber)
        )
        validateFormOnInput()
    }

    private fun handleRoomNumberInputValueChange(roomNumber: String) {
        if (!roomNumber.isDigitsOnly()) return

        viewState = viewState.copy(
            roomNumber = viewState.roomNumber.updateValue(roomNumber)
        )
        validateFormOnInput()
    }

    private fun handleBedsCountInputValueChange(bedsCount: String) {
        if (!bedsCount.isDigitsOnly()) return

        viewState = viewState.copy(
            bedsSelection = viewState.bedsSelection.updateTextField(bedsCount)
        )
        validateFormOnInput()
    }

    private fun handleBedsCountSelect(bedsCount: Int) {
        viewState = viewState.copy(
            bedsSelection = viewState.bedsSelection.selectBedsCount(bedsCount)
        )
        validateFormOnInput()
    }

    private fun handleToggleFloorInput() {
        viewState = viewState.copy(
            floorSelection = viewState.floorSelection.toggleInput()
        )
    }

    private fun handleToggleBedsInput() {
        viewState = viewState.copy(
            bedsSelection = viewState.bedsSelection.toggleInput()
        )
    }

    private fun handleOnCreateRoomClick() {
        viewModelScope.launch {
            try {
                viewState = viewState.copy(isLoading = true)

                if (!performValidation()) {
                    viewState = viewState.copy(isLoading = false)
                    return@launch
                }

                val floorNumber = viewState.floorSelection.textField.value.toInt()
                val roomNumber = viewState.roomNumber.value.toInt()
                val bedsCount = viewState.bedsSelection.textField.value.toInt()

                val existingRooms = roomsRepository.getAllRooms().getOrElse { emptyList() }
                val roomExists = existingRooms.any { room ->
                    room.floorNumber == floorNumber && room.roomNumber == roomNumber
                }

                if (roomExists) {
                    viewState = viewState.copy(isLoading = false)
                    val errorMessage = getString(
                        Res.string.add_room_error_room_already_exists,
                        roomNumber,
                        floorNumber
                    )
                    viewAction = AddRoomAction.ShowErrorMessage(errorMessage)
                    return@launch
                }

                val newRoom = Room(
                    floorNumber = floorNumber,
                    roomNumber = roomNumber,
                    bedsCount = bedsCount,
                    students = emptyList()
                )

                val result = roomsRepository.saveRoom(newRoom)

                result.fold(
                    onSuccess = {
                        viewState = viewState.copy(isLoading = false)
                        val message = getString(
                            Res.string.add_room_success_message,
                            roomNumber
                        )
                        viewAction = AddRoomAction.ShowSuccessMessage(message)
                        viewAction = AddRoomAction.CloseScreen
                    },
                    onFailure = { error ->
                        viewState = viewState.copy(isLoading = false)
                        val errorMessage = getString(Res.string.add_room_error_save_failed)
                        viewAction = AddRoomAction.ShowErrorMessage(errorMessage)
                    }
                )
            } catch (_: Exception) {
                viewState = viewState.copy(isLoading = false)
                val errorMessage = getString(Res.string.add_room_error_unknown)
                viewAction = AddRoomAction.ShowErrorMessage(errorMessage)
            }
        }
    }

    private fun handleOnCancelClick() {
        viewAction = AddRoomAction.CloseScreen
    }

    private fun validateFormOnInput() {
        viewModelScope.launch {
            var isValid = true
            val currentState = viewState

            val floorNumberError = validateFloorNumber(currentState.floorSelection.textField.value)
            val roomNumberError = validateRoomNumber(currentState.roomNumber.value)
            val bedsCountError = validateBedsCount(currentState.bedsSelection.textField.value)

            if (floorNumberError != null) isValid = false
            if (roomNumberError != null) isValid = false
            if (bedsCountError != null) isValid = false

            viewState = viewState.copy(isFormValid = isValid)
        }
    }

    private suspend fun performValidation(): Boolean {
        var isValid = true
        val currentState = viewState

        val floorNumberError = validateFloorNumber(currentState.floorSelection.textField.value)
        val roomNumberError = validateRoomNumber(currentState.roomNumber.value)
        val bedsCountError = validateBedsCount(currentState.bedsSelection.textField.value)

        if (floorNumberError != null) isValid = false
        if (roomNumberError != null) isValid = false
        if (bedsCountError != null) isValid = false

        viewState = viewState.copy(
            floorSelection = viewState.floorSelection.updateTextFieldError(
                isError = floorNumberError != null,
                errorMessage = floorNumberError
            ),
            roomNumber = viewState.roomNumber.updateError(
                isError = roomNumberError != null,
                errorMessage = roomNumberError
            ),
            bedsSelection = viewState.bedsSelection.updateTextFieldError(
                isError = bedsCountError != null,
                errorMessage = bedsCountError
            ),
            isFormValid = isValid
        )

        return isValid
    }

    private suspend fun validateFloorNumber(floorNumber: String): String? {
        return when {
            floorNumber.isBlank() -> getString(Res.string.add_room_validation_floor_number_required)
            else -> null
        }
    }

    private suspend fun validateRoomNumber(roomNumber: String): String? {
        return when {
            roomNumber.isBlank() -> getString(Res.string.add_room_validation_room_number_required)
            else -> null
        }
    }

    private suspend fun validateBedsCount(bedsCount: String): String? {
        return when {
            bedsCount.isBlank() -> getString(Res.string.add_room_validation_beds_count_required)
            else -> null
        }
    }

    private fun TextFieldState.updateValue(value: String): TextFieldState = copy(
        value = value,
        isError = false,
        errorMessage = null
    )

    private fun TextFieldState.updateError(
        isError: Boolean,
        errorMessage: String?
    ): TextFieldState =
        copy(
            isError = isError,
            errorMessage = errorMessage
        )

    private fun FloorSelectionState.updateTextField(value: String): FloorSelectionState = copy(
        textField = textField.updateValue(value)
    )

    private fun FloorSelectionState.updateTextFieldError(
        isError: Boolean,
        errorMessage: String?
    ): FloorSelectionState = copy(
        textField = textField.updateError(isError, errorMessage)
    )

    private fun FloorSelectionState.toggleInput(): FloorSelectionState = copy(
        showInput = !showInput,
        textField = if (showInput) textField.copy(value = "") else textField
    )

    private fun FloorSelectionState.selectFloor(floor: Int): FloorSelectionState = copy(
        textField = textField.updateValue(floor.toString()),
        showInput = false
    )

    private fun BedsSelectionState.updateTextField(value: String): BedsSelectionState = copy(
        textField = textField.updateValue(value)
    )

    private fun BedsSelectionState.updateTextFieldError(
        isError: Boolean,
        errorMessage: String?
    ): BedsSelectionState = copy(
        textField = textField.updateError(isError, errorMessage)
    )

    private fun BedsSelectionState.toggleInput(): BedsSelectionState = copy(
        showInput = !showInput,
        textField = if (showInput) textField.copy(value = "") else textField
    )

    private fun BedsSelectionState.selectBedsCount(bedsCount: Int): BedsSelectionState = copy(
        textField = textField.updateValue(bedsCount.toString()),
        showInput = false
    )
}
