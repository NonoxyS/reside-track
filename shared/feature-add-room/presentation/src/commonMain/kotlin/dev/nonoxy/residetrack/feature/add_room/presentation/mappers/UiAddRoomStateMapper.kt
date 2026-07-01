package dev.nonoxy.residetrack.feature.add_room.presentation.mappers

import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.residetrack.feature.add_room.presentation.models.UiAddRoomState
import kotlinx.collections.immutable.toImmutableList

internal interface UiAddRoomStateMapper {
    fun map(item: AddRoomStore.State): UiAddRoomState
}

internal class UiAddRoomStateMapperImpl : UiAddRoomStateMapper {

    override fun map(item: AddRoomStore.State): UiAddRoomState = UiAddRoomState(
        floorSelection = UiAddRoomState.FloorSelection(
            textField = UiAddRoomState.TextField(
                value = item.floorSelection.textField.value,
                errorKind = item.floorSelection.textField.errorKind,
            ),
            existingFloors = item.floorSelection.existingFloors.toImmutableList(),
            showInput = item.floorSelection.showInput,
        ),
        roomNumber = UiAddRoomState.TextField(
            value = item.roomNumber.value,
            errorKind = item.roomNumber.errorKind,
        ),
        bedsSelection = UiAddRoomState.BedsSelection(
            textField = UiAddRoomState.TextField(
                value = item.bedsSelection.textField.value,
                errorKind = item.bedsSelection.textField.errorKind,
            ),
            existingBedsCounts = item.bedsSelection.existingBedsCounts.toImmutableList(),
            showInput = item.bedsSelection.showInput,
        ),
        isLoading = item.isLoading,
        isFormValid = item.isFormValid,
        hasExistingRooms = item.hasExistingRooms,
        isDirty = item.isDirty,
        showDiscardConfirm = item.showDiscardConfirm,
    )
}
