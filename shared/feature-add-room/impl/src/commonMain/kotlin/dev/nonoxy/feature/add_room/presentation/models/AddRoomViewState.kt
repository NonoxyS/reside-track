package dev.nonoxy.feature.add_room.presentation.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class TextFieldState(
    val value: String = "",
    val isError: Boolean = false,
    val errorMessage: String? = null
)

internal data class FloorSelectionState(
    val textField: TextFieldState = TextFieldState(),
    val existingFloors: ImmutableList<Int> = persistentListOf(),
    val showInput: Boolean = false
)

internal data class BedsSelectionState(
    val textField: TextFieldState = TextFieldState(),
    val existingBedsCounts: ImmutableList<Int> = persistentListOf(),
    val showInput: Boolean = false
)

internal data class AddRoomViewState(
    val floorSelection: FloorSelectionState,
    val roomNumber: TextFieldState,
    val bedsSelection: BedsSelectionState,
    val isLoading: Boolean,
    val isFormValid: Boolean,
    val hasExistingRooms: Boolean
) {
    companion object {
        val Initial = AddRoomViewState(
            floorSelection = FloorSelectionState(),
            roomNumber = TextFieldState(),
            bedsSelection = BedsSelectionState(),
            isLoading = false,
            isFormValid = false,
            hasExistingRooms = false
        )
    }
}
