package dev.nonoxy.feature.add_room.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.State
import dev.nonoxy.feature.add_room.impl.domain.AddRoomStoreFactory.Message

internal class AddRoomReducer : Reducer<State, Message> {

    override fun State.reduce(msg: Message): State = when (msg) {
        is Message.SetExistingRoomsData -> copy(
            floorSelection = floorSelection.copy(existingFloors = msg.existingFloors),
            bedsSelection = bedsSelection.copy(existingBedsCounts = msg.existingBedsCounts),
            hasExistingRooms = true,
        )

        is Message.SetFloorNumberInput -> copy(
            floorSelection = floorSelection.copy(
                textField = floorSelection.textField.copy(value = msg.value, errorKind = null),
            ),
        )

        is Message.SetFloorNumberSelected -> copy(
            floorSelection = floorSelection.copy(
                textField = floorSelection.textField.copy(
                    value = msg.floor.toString(),
                    errorKind = null,
                ),
                showInput = false,
            ),
        )

        Message.ToggleFloorInput -> copy(
            floorSelection = floorSelection.copy(
                showInput = !floorSelection.showInput,
                textField = if (floorSelection.showInput) {
                    floorSelection.textField.copy(
                        value = ""
                    )
                } else {
                    floorSelection.textField
                },
            ),
        )

        is Message.SetRoomNumberInput -> copy(
            roomNumber = roomNumber.copy(value = msg.value, errorKind = null),
        )

        is Message.SetBedsCountInput -> copy(
            bedsSelection = bedsSelection.copy(
                textField = bedsSelection.textField.copy(value = msg.value, errorKind = null),
            ),
        )

        is Message.SetBedsCountSelected -> copy(
            bedsSelection = bedsSelection.copy(
                textField = bedsSelection.textField.copy(
                    value = msg.bedsCount.toString(),
                    errorKind = null,
                ),
                showInput = false,
            ),
        )

        Message.ToggleBedsInput -> copy(
            bedsSelection = bedsSelection.copy(
                showInput = !bedsSelection.showInput,
                textField = if (bedsSelection.showInput) {
                    bedsSelection.textField.copy(
                        value = ""
                    )
                } else {
                    bedsSelection.textField
                },
            ),
        )

        is Message.SetValidationErrors -> copy(
            floorSelection = floorSelection.copy(
                textField = floorSelection.textField.copy(errorKind = msg.floorNumberError.value),
            ),
            roomNumber = roomNumber.copy(errorKind = msg.roomNumberError.value),
            bedsSelection = bedsSelection.copy(
                textField = bedsSelection.textField.copy(errorKind = msg.bedsCountError.value),
            ),
        )

        is Message.SetIsFormValid -> copy(isFormValid = msg.isValid)
        is Message.SetIsLoading -> copy(isLoading = msg.isLoading)
    }
}
