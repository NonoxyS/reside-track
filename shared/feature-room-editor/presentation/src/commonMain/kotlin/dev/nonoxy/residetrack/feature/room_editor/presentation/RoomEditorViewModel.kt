package dev.nonoxy.residetrack.feature.room_editor.presentation

import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import dev.nonoxy.residetrack.core.presentation.snackbar.SnackbarBus
import dev.nonoxy.residetrack.core.presentation.snackbar.SnackbarEvent
import dev.nonoxy.residetrack.core.presentation.snackbar.SnackbarEventType
import dev.nonoxy.residetrack.core.presentation.viewmodel.BaseViewModel
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.DateField
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.Intent
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorSuccessKind
import dev.nonoxy.residetrack.feature.room_editor.presentation.mappers.UiRoomEditorLabelMapper
import dev.nonoxy.residetrack.feature.room_editor.presentation.mappers.UiRoomEditorStateMapper
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiDateField
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiRoomEditorLabel
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiRoomEditorState
import dev.nonoxy.residetrack.res.MR
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

class RoomEditorViewModel internal constructor(
    private val store: RoomEditorStore,
    private val stateMapper: UiRoomEditorStateMapper,
    private val labelMapper: UiRoomEditorLabelMapper,
    private val snackbarBus: SnackbarBus,
) : BaseViewModel<UiRoomEditorState, UiRoomEditorLabel>(initialState = UiRoomEditorState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
        viewModelScope.launch {
            store.labels
                .filterIsInstance<RoomEditorStore.Label.ShowSuccess>()
                .collect { label ->
                    snackbarBus.send(
                        SnackbarEvent(
                            message = label.kind.toStringResource(),
                            type = SnackbarEventType.SUCCESS,
                        )
                    )
                }
        }
    }

    fun onRetryLoadStudents() = store.accept(Intent.LoadStudents)

    fun onAddStudent() = store.accept(Intent.OnAddStudent)

    fun onRemoveStudent(studentId: String) =
        store.accept(Intent.OnRemoveStudentRequested(studentId = studentId))

    fun onRemoveStudentConfirmed() = store.accept(Intent.OnRemoveStudentConfirmed)

    fun onRemoveStudentDismissed() = store.accept(Intent.OnRemoveStudentDismissed)

    fun onStreamNumberChange(studentId: String, value: String) =
        store.accept(Intent.OnStreamNumberChange(studentId = studentId, value = value))

    fun onCheckInDateChange(studentId: String, value: String) =
        store.accept(Intent.OnCheckInDateChange(studentId = studentId, value = value))

    fun onCheckOutDateChange(studentId: String, value: String) =
        store.accept(Intent.OnCheckOutDateChange(studentId = studentId, value = value))

    fun onCheckInDateMillisChange(studentId: String, millis: Long) =
        store.accept(Intent.OnCheckInDateMillisChange(studentId = studentId, millis = millis))

    fun onCheckOutDateMillisChange(studentId: String, millis: Long) =
        store.accept(Intent.OnCheckOutDateMillisChange(studentId = studentId, millis = millis))

    fun onDatePickerOpen(studentId: String, field: UiDateField) =
        store.accept(Intent.OnDatePickerOpen(studentId = studentId, field = field.toDomain()))

    fun onDatePickerDismiss() = store.accept(Intent.OnDatePickerDismiss)

    fun onSaveAndClose() = store.accept(Intent.OnSaveAndClose)

    fun onClose() = store.accept(Intent.OnClose)

    fun onDismissRequested() = store.accept(Intent.OnDismissRequested)

    fun onDiscardConfirmed() = store.accept(Intent.OnDiscardConfirmed)

    fun onKeepEditing() = store.accept(Intent.OnKeepEditing)

    fun onEditRoomParamsClick() = store.accept(Intent.OnEditRoomParamsClick)
    fun onRoomParamsDismiss() = store.accept(Intent.OnRoomParamsDismiss)
    fun onRoomParamsFloorChange(value: String) = store.accept(Intent.OnRoomParamsFloorChange(value))
    fun onRoomParamsRoomNumberChange(value: String) = store.accept(Intent.OnRoomParamsRoomNumberChange(value))
    fun onRoomParamsBedsChange(value: String) = store.accept(Intent.OnRoomParamsBedsChange(value))
    fun onSaveRoomParams() = store.accept(Intent.OnSaveRoomParams)
    fun onDeleteRoomClick() = store.accept(Intent.OnDeleteRoomClick)
    fun onDeleteRoomConfirm() = store.accept(Intent.OnDeleteRoomConfirm)
    fun onDeleteRoomDismiss() = store.accept(Intent.OnDeleteRoomDismiss)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }

    private fun UiDateField.toDomain(): DateField = when (this) {
        UiDateField.CHECK_IN -> DateField.CHECK_IN
        UiDateField.CHECK_OUT -> DateField.CHECK_OUT
    }

    private fun RoomEditorSuccessKind.toStringResource() = when (this) {
        RoomEditorSuccessKind.StudentsSaved -> MR.strings.students_saved_successfully
        RoomEditorSuccessKind.RoomUpdated -> MR.strings.room_updated_successfully
        RoomEditorSuccessKind.RoomDeleted -> MR.strings.room_deleted_successfully
    }
}
