package dev.nonoxy.residetrack.feature.manage_students.presentation.models

import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsErrorKind
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class UiManageStudentsState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorKind: ManageStudentsErrorKind? = null,
    val room: UiRoom? = null,
    val editableStudents: ImmutableList<UiEditableStudent> = persistentListOf(),
    val isDirty: Boolean = false,
    val showDiscardConfirm: Boolean = false,
    val openDatePicker: UiOpenDatePicker? = null,
)
