package dev.nonoxy.residetrack.feature.room_editor.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.State
import dev.nonoxy.residetrack.feature.room_editor.impl.domain.RoomEditorStoreFactory.Message

internal class RoomEditorReducer : Reducer<State, Message> {

    override fun State.reduce(msg: Message): State = when (msg) {
        is Message.SetIsLoading -> copy(
            isLoading = msg.isLoading,
            isError = if (msg.isLoading) false else isError,
            errorKind = if (msg.isLoading) null else errorKind,
        )

        is Message.SetError -> copy(
            isLoading = false,
            isError = true,
            errorKind = msg.kind,
        )

        Message.ClearError -> copy(
            isError = false,
            errorKind = null,
        )

        is Message.SetRoomAndStudents -> copy(
            isLoading = false,
            isError = false,
            errorKind = null,
            room = msg.room,
            editableStudents = msg.editableStudents,
            initialStudentCount = msg.editableStudents.size,
            isDirty = false,
            isSaveEnabled = false,
            isAddStudentEnabled = msg.editableStudents.size < msg.room.bedsCount,
            showDiscardConfirm = false,
            removingStudentId = null,
        )

        is Message.SetEditableStudents -> copy(
            editableStudents = msg.editableStudents,
            isDirty = true,
            isSaveEnabled = msg.editableStudents.isNotEmpty() || initialStudentCount > 0,
            isAddStudentEnabled = msg.editableStudents.size < (room?.bedsCount ?: Int.MAX_VALUE),
        )

        is Message.SetShowDiscardConfirm -> copy(
            showDiscardConfirm = msg.show,
        )

        is Message.SetOpenDatePicker -> copy(
            openDatePicker = msg.openPicker,
        )

        is Message.SetShowRoomParams -> copy(showRoomParams = msg.show)
        is Message.SetRoomParams -> copy(roomParams = msg.params)
        is Message.SetShowDeleteConfirm -> copy(showDeleteConfirm = msg.show)
        is Message.SetRoom -> copy(
            room = msg.room,
            isAddStudentEnabled = editableStudents.size < msg.room.bedsCount,
        )
        is Message.SetRemovingStudentId -> copy(removingStudentId = msg.studentId)
    }
}
