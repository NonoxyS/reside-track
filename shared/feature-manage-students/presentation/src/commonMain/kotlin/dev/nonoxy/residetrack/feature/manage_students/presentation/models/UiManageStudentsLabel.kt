package dev.nonoxy.residetrack.feature.manage_students.presentation.models

import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsErrorKind
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsSuccessKind

sealed interface UiManageStudentsLabel {
    data object NavigateBack : UiManageStudentsLabel
    data class ShowError(val kind: ManageStudentsErrorKind) : UiManageStudentsLabel
    data class ShowSuccess(val kind: ManageStudentsSuccessKind) : UiManageStudentsLabel
}
