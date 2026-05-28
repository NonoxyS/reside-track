package dev.nonoxy.residetrack.feature.manage_students.presentation.mappers

import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiEditableStudent
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiManageStudentsState
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
    )
}
