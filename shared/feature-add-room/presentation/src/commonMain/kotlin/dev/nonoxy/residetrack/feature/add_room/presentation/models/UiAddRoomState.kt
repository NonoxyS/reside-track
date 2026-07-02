package dev.nonoxy.residetrack.feature.add_room.presentation.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class UiAddRoomState(
    val floorSelection: FloorSelection = FloorSelection(),
    val roomNumber: TextField = TextField(),
    val bedsSelection: BedsSelection = BedsSelection(),
    val isLoading: Boolean = false,
    val isFormValid: Boolean = false,
    val hasExistingRooms: Boolean = false,
    val isDirty: Boolean = false,
    val showDiscardConfirm: Boolean = false,
) {
    data class TextField(
        val value: String = "",
        val error: UiAddRoomFieldError? = null,
    )

    data class FloorSelection(
        val textField: TextField = TextField(),
        val existingFloors: ImmutableList<Int> = persistentListOf(),
        val showInput: Boolean = false,
    )

    data class BedsSelection(
        val textField: TextField = TextField(),
        val existingBedsCounts: ImmutableList<Int> = persistentListOf(),
        val showInput: Boolean = false,
    )
}
