package dev.nonoxy.residetrack.feature.manage_students.presentation.mappers

import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore.DateField
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiDateField
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiEditableStudent
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiManageStudentsState
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiOpenDatePicker
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiRoomParams
import kotlinx.collections.immutable.toPersistentList

internal interface UiManageStudentsStateMapper {
    fun map(item: ManageStudentsStore.State): UiManageStudentsState
}

internal class UiManageStudentsStateMapperImpl(
    private val uiRoomMapper: UiRoomMapper,
) : UiManageStudentsStateMapper {

    override fun map(item: ManageStudentsStore.State): UiManageStudentsState = UiManageStudentsState(
        isLoading = item.isLoading,
        isError = item.isError,
        errorKind = item.errorKind,
        room = item.room?.let(uiRoomMapper::map),
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
    )

    private fun DateField.toUi(): UiDateField = when (this) {
        DateField.CHECK_IN -> UiDateField.CHECK_IN
        DateField.CHECK_OUT -> UiDateField.CHECK_OUT
    }
}
