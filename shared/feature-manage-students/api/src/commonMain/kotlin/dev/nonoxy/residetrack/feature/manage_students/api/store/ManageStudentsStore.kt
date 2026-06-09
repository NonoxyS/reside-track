package dev.nonoxy.residetrack.feature.manage_students.api.store

import com.arkivanov.mvikotlin.core.store.Store
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore.Intent
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore.Label
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore.State
import dev.nonoxy.residetrack.core.rooms.models.Room
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

interface ManageStudentsStore : Store<Intent, State, Label> {

    data class State(
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        val errorKind: ManageStudentsErrorKind? = null,
        val room: Room? = null,
        val editableStudents: ImmutableList<EditableStudent> = persistentListOf(),
        val isDirty: Boolean = false,
        val showDiscardConfirm: Boolean = false,
        val openDatePicker: OpenPicker? = null,
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
    }

    enum class DateField { CHECK_IN, CHECK_OUT }

    sealed interface Intent {
        data object LoadStudents : Intent
        data object OnAddStudent : Intent
        data class OnRemoveStudent(val studentId: String) : Intent
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
    }

    sealed interface Label {
        data object NavigateBack : Label
        data class ShowError(val kind: ManageStudentsErrorKind) : Label
        data class ShowSuccess(val kind: ManageStudentsSuccessKind) : Label
    }
}

sealed interface ManageStudentsErrorKind {
    data object FailedToLoadStudents : ManageStudentsErrorKind
    data object FailedToSaveStudents : ManageStudentsErrorKind
    data object RoomNotFound : ManageStudentsErrorKind
    data object DraftRoomNotFound : ManageStudentsErrorKind
    data object StreamNumberInvalid : ManageStudentsErrorKind
    data object InvalidDateRange : ManageStudentsErrorKind
    data object InvalidDateFormat : ManageStudentsErrorKind
    data object DuplicateStreamNumbers : ManageStudentsErrorKind
}

sealed interface ManageStudentsSuccessKind {
    data object StudentsSaved : ManageStudentsSuccessKind
}
