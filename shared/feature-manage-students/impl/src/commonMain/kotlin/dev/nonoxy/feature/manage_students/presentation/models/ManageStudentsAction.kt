package dev.nonoxy.feature.manage_students.presentation.models

internal sealed interface ManageStudentsAction {
    data object NavigateBack : ManageStudentsAction
    data class ShowError(val message: String) : ManageStudentsAction
    data class ShowSuccess(val message: String) : ManageStudentsAction
} 