package dev.nonoxy.residetrack.feature.manage_students.presentation.models

sealed interface UiManageStudentsLabel {
    data object NavigateBack : UiManageStudentsLabel
    data class ShowError(val message: String) : UiManageStudentsLabel
    data class ShowSuccess(val message: String) : UiManageStudentsLabel
}
