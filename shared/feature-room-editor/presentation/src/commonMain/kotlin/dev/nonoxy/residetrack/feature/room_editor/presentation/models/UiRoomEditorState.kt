package dev.nonoxy.residetrack.feature.room_editor.presentation.models

import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorErrorKind
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class UiRoomEditorState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorKind: RoomEditorErrorKind? = null,
    val room: UiRoom? = null,
    val editableStudents: ImmutableList<UiEditableStudent> = persistentListOf(),
    val initialStudentCount: Int = 0,
    val isSaveEnabled: Boolean = false,
    val isAddStudentEnabled: Boolean = true,
    val isDirty: Boolean = false,
    val showDiscardConfirm: Boolean = false,
    val openDatePicker: UiOpenDatePicker? = null,
    val showRoomParams: Boolean = false,
    val roomParams: UiRoomParams? = null,
    val showDeleteConfirm: Boolean = false,
    val removingStudentId: String? = null,
)
