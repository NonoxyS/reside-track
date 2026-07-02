package dev.nonoxy.residetrack.feature.add_room.api.store

import com.arkivanov.mvikotlin.core.store.Store
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore.Intent
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore.Label
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore.State

interface AddRoomStore : Store<Intent, State, Label> {

    data class State(
        val floorSelection: FloorSelectionState = FloorSelectionState(),
        val roomNumber: TextFieldState = TextFieldState(),
        val bedsSelection: BedsSelectionState = BedsSelectionState(),
        val isLoading: Boolean = false,
        val isFormValid: Boolean = false,
        val hasExistingRooms: Boolean = false,
        val showDiscardConfirm: Boolean = false,
    ) {
        /** Any user-entered input present — used to guard accidental dismiss. */
        val isDirty: Boolean
            get() = floorSelection.textField.value.isNotBlank() ||
                roomNumber.value.isNotBlank() ||
                bedsSelection.textField.value.isNotBlank() ||
                floorSelection.showInput ||
                bedsSelection.showInput

        data class TextFieldState(
            val value: String = "",
            val errorKind: AddRoomErrorKind? = null,
        )

        data class FloorSelectionState(
            val textField: TextFieldState = TextFieldState(),
            val existingFloors: List<Int> = emptyList(),
            val showInput: Boolean = false,
        )

        data class BedsSelectionState(
            val textField: TextFieldState = TextFieldState(),
            val existingBedsCounts: List<Int> = emptyList(),
            val showInput: Boolean = false,
        )
    }

    sealed interface Intent {
        data class OnFloorNumberInputValueChange(val floorNumber: String) : Intent
        data class OnFloorNumberSelect(val floorNumber: Int) : Intent
        data class OnRoomNumberInputValueChange(val roomNumber: String) : Intent
        data class OnBedsCountInputValueChange(val bedsCount: String) : Intent
        data class OnBedsCountSelect(val bedsCount: Int) : Intent
        data object OnCreateRoomClick : Intent
        data object OnCancelClick : Intent
        data object OnToggleFloorInput : Intent
        data object OnToggleBedsInput : Intent
        data object OnDismissRequested : Intent
        data object OnDiscardConfirmed : Intent
        data object OnKeepEditing : Intent
    }

    sealed interface Label {
        data object CloseScreen : Label
        data object NavigateToRoomEditorDraftRoom : Label
        data class ShowSuccess(val kind: AddRoomSuccessKind) : Label
        data class ShowError(val kind: AddRoomErrorKind) : Label
    }
}
