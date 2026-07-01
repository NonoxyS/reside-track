package dev.nonoxy.residetrack.feature.add_room.presentation.mappers

import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomErrorKind
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.residetrack.feature.add_room.presentation.models.UiAddRoomFieldError
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
                error = item.floorSelection.textField.errorKind.toFieldError(),
            ),
            existingFloors = item.floorSelection.existingFloors.toImmutableList(),
            showInput = item.floorSelection.showInput,
        ),
        roomNumber = UiAddRoomState.TextField(
            value = item.roomNumber.value,
            error = item.roomNumber.errorKind.toFieldError(),
        ),
        bedsSelection = UiAddRoomState.BedsSelection(
            textField = UiAddRoomState.TextField(
                value = item.bedsSelection.textField.value,
                error = item.bedsSelection.textField.errorKind.toFieldError(),
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

    // Only the argument-free "required" kinds ever surface on a field; the rest
    // (save failed, duplicate room, unknown) are delivered as a snackbar label.
    private fun AddRoomErrorKind?.toFieldError(): UiAddRoomFieldError? = when (this) {
        AddRoomErrorKind.FloorNumberRequired -> UiAddRoomFieldError.FLOOR_REQUIRED
        AddRoomErrorKind.RoomNumberRequired -> UiAddRoomFieldError.ROOM_NUMBER_REQUIRED
        AddRoomErrorKind.BedsCountRequired -> UiAddRoomFieldError.BEDS_REQUIRED
        else -> null
    }
}
