package dev.nonoxy.residetrack.feature.add_room.presentation.models

import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomErrorKind
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class UiAddRoomState(
    val floorSelection: FloorSelection = FloorSelection(),
    val roomNumber: TextField = TextField(),
    val bedsSelection: BedsSelection = BedsSelection(),
    val isLoading: Boolean = false,
    val isFormValid: Boolean = false,
    val hasExistingRooms: Boolean = false,
) {
    data class TextField(
        val value: String = "",
        val errorKind: AddRoomErrorKind? = null,
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
