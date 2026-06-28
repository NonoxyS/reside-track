package dev.nonoxy.residetrack.feature.room_editor.api.store

import com.arkivanov.mvikotlin.core.store.Store
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.Intent
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.Label
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.State
import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

interface RoomEditorStore : Store<Intent, State, Label> {

    data class State(
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        val errorKind: RoomEditorErrorKind? = null,
        val room: Room? = null,
        val editableStudents: ImmutableList<EditableStudent> = persistentListOf(),
        val isDirty: Boolean = false,
        val showDiscardConfirm: Boolean = false,
        val openDatePicker: OpenPicker? = null,
        val showRoomParams: Boolean = false,
        val roomParams: RoomParams? = null,
        val showDeleteConfirm: Boolean = false,
        val removingStudentId: String? = null,
    ) {
        data class EditableStudent(
            val id: String,
            val studentId: Long?,
            val streamNumber: String,
            val checkInDate: String,
            val checkOutDate: String,
            val checkInDateMillis: Long? = null,
            val checkOutDateMillis: Long? = null,
            val isNew: Boolean = false,
        )

        /** Identifies the single date picker currently open on the screen. */
        data class OpenPicker(val studentId: String, val field: DateField)

        /** Editable buffer for the room-params sheet (separate from saved [room]). */
        data class RoomParams(
            val floorNumber: String,
            val roomNumber: String,
            val bedsCount: String,
            val roomNumberError: Boolean = false,
        )
    }

    enum class DateField { CHECK_IN, CHECK_OUT }

    sealed interface Intent {
        data object LoadStudents : Intent
        data object OnAddStudent : Intent
        data class OnRemoveStudentRequested(val studentId: String) : Intent
        data object OnRemoveStudentConfirmed : Intent
        data object OnRemoveStudentDismissed : Intent
        data class OnStreamNumberChange(val studentId: String, val value: String) : Intent
        data class OnCheckInDateChange(val studentId: String, val value: String) : Intent
        data class OnCheckOutDateChange(val studentId: String, val value: String) : Intent
        data class OnCheckInDateMillisChange(val studentId: String, val millis: Long) : Intent
        data class OnCheckOutDateMillisChange(val studentId: String, val millis: Long) : Intent
        data class OnDatePickerOpen(val studentId: String, val field: DateField) : Intent
        data object OnDatePickerDismiss : Intent
        data object OnSaveAndClose : Intent
        data object OnClose : Intent
        data object OnDismissRequested : Intent
        data object OnDiscardConfirmed : Intent
        data object OnKeepEditing : Intent
        data object OnEditRoomParamsClick : Intent
        data object OnRoomParamsDismiss : Intent
        data class OnRoomParamsFloorChange(val value: String) : Intent
        data class OnRoomParamsRoomNumberChange(val value: String) : Intent
        data class OnRoomParamsBedsChange(val value: String) : Intent
        data object OnSaveRoomParams : Intent
        data object OnDeleteRoomClick : Intent
        data object OnDeleteRoomConfirm : Intent
        data object OnDeleteRoomDismiss : Intent
    }

    sealed interface Label {
        data object NavigateBack : Label
        data class ShowError(val kind: RoomEditorErrorKind) : Label
        data class ShowSuccess(val kind: RoomEditorSuccessKind) : Label
    }
}

sealed interface RoomEditorErrorKind {
    data object FailedToLoadStudents : RoomEditorErrorKind
    data object FailedToSaveStudents : RoomEditorErrorKind
    data object RoomNotFound : RoomEditorErrorKind
    data object DraftRoomNotFound : RoomEditorErrorKind
    data object StreamNumberInvalid : RoomEditorErrorKind
    data object InvalidDateRange : RoomEditorErrorKind
    data object InvalidDateFormat : RoomEditorErrorKind
    data object DuplicateStreamNumbers : RoomEditorErrorKind
    data object RoomNumberTaken : RoomEditorErrorKind
    data object FailedToUpdateRoom : RoomEditorErrorKind
    data object FailedToDeleteRoom : RoomEditorErrorKind
}

sealed interface RoomEditorSuccessKind {
    data object StudentsSaved : RoomEditorSuccessKind
    data object RoomUpdated : RoomEditorSuccessKind
    data object RoomDeleted : RoomEditorSuccessKind
}
