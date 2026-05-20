package dev.nonoxy.feature.manage_students.presentation.models

import dev.nonoxy.feature.manage_students.ui.models.UiRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class EditableStudent(
    val id: String,
    val studentId: Long?,
    val streamNumber: String,
    val checkInDate: String,
    val checkOutDate: String,
    val checkInDateMillis: Long? = null,
    val checkOutDateMillis: Long? = null,
    val isNew: Boolean = false
)

internal data class ManageStudentsViewState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val room: UiRoom? = null,
    val editableStudents: ImmutableList<EditableStudent> = persistentListOf()
) 