package dev.nonoxy.residetrack.feature.manage_students.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore.DateField
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore.Intent
import dev.nonoxy.residetrack.feature.manage_students.presentation.mappers.UiManageStudentsLabelMapper
import dev.nonoxy.residetrack.feature.manage_students.presentation.mappers.UiManageStudentsStateMapper
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiDateField
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiManageStudentsLabel
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiManageStudentsState
import dev.nonoxy.residetrack.core.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.mapNotNull

class ManageStudentsViewModel internal constructor(
    private val store: ManageStudentsStore,
    private val stateMapper: UiManageStudentsStateMapper,
    private val labelMapper: UiManageStudentsLabelMapper,
) : BaseViewModel<UiManageStudentsState, UiManageStudentsLabel>(initialState = UiManageStudentsState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onRetryLoadStudents() = store.accept(Intent.LoadStudents)

    fun onAddStudent() = store.accept(Intent.OnAddStudent)

    fun onRemoveStudent(studentId: String) = store.accept(Intent.OnRemoveStudent(studentId = studentId))

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
}
