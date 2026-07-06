package dev.nonoxy.residetrack.feature.room_editor.presentation.mappers

import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.DateField
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiDateField
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiEditableStudent
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiRoomEditorState
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiOpenDatePicker
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiRoomParams
import kotlinx.collections.immutable.toPersistentList

internal interface UiRoomEditorStateMapper {
    fun map(item: RoomEditorStore.State): UiRoomEditorState
}

internal class UiRoomEditorStateMapperImpl(
    private val uiRoomMapper: UiRoomMapper,
) : UiRoomEditorStateMapper {

    override fun map(item: RoomEditorStore.State): UiRoomEditorState = UiRoomEditorState(
        isLoading = item.isLoading,
        isError = item.isError,
        errorKind = item.errorKind,
        room = item.room?.let(uiRoomMapper::map),
        isSaveEnabled = item.isSaveEnabled,
        isAddStudentEnabled = item.isAddStudentEnabled,
        editableStudents = item.editableStudents.map { st ->
            UiEditableStudent(
                id = st.id,
                studentId = st.studentId,
                streamNumber = st.streamNumber,
                checkInDate = st.checkInDate,
                checkOutDate = st.checkOutDate,
                checkInDateMillis = st.checkInDateMillis,
                checkOutDateMillis = st.checkOutDateMillis,
                isNew = st.isNew,
            )
        }.toPersistentList(),
        isDirty = item.isDirty,
        showDiscardConfirm = item.showDiscardConfirm,
        openDatePicker = item.openDatePicker?.let { open ->
            UiOpenDatePicker(studentId = open.studentId, field = open.field.toUi())
        },
        showRoomParams = item.showRoomParams,
        roomParams = item.roomParams?.let {
            UiRoomParams(it.floorNumber, it.roomNumber, it.bedsCount, it.roomNumberError)
        },
        showDeleteConfirm = item.showDeleteConfirm,
        removingStudentId = item.removingStudentId,
    )

    private fun DateField.toUi(): UiDateField = when (this) {
        DateField.CHECK_IN -> UiDateField.CHECK_IN
        DateField.CHECK_OUT -> UiDateField.CHECK_OUT
    }
}
