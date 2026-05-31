package dev.nonoxy.residetrack.feature.manage_students.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiManageStudentsState
import dev.nonoxy.residetrack.common.ui.common.state.ShowStateData

@Composable
internal fun ManageStudentsContent(
    state: UiManageStudentsState,
    onRetryClick: () -> Unit,
    onCloseClick: () -> Unit,
    onAddStudent: () -> Unit,
    onRemoveStudent: (String) -> Unit,
    onStreamNumberChange: (String, String) -> Unit,
    onCheckInDateChange: (String, String) -> Unit,
    onCheckOutDateChange: (String, String) -> Unit,
    onCheckInDateMillisChange: (String, Long) -> Unit,
    onCheckOutDateMillisChange: (String, Long) -> Unit,
    onSaveAndClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ShowStateData(
        modifier = modifier,
        state = state,
        isLoading = state.isLoading,
        isError = state.isError,
        onRetryClick = onRetryClick,
    ) { currentState ->
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            ManageStudentsTopBar(
                room = currentState.room,
                onCloseClick = onCloseClick,
            )

            ManageStudentsList(
                students = currentState.editableStudents,
                onAddStudent = onAddStudent,
                onRemoveStudent = onRemoveStudent,
                onStreamNumberChange = onStreamNumberChange,
                onCheckInDateChange = onCheckInDateChange,
                onCheckOutDateChange = onCheckOutDateChange,
                onCheckInDateMillisChange = onCheckInDateMillisChange,
                onCheckOutDateMillisChange = onCheckOutDateMillisChange,
                modifier = Modifier.weight(1f),
            )

            ManageStudentsBottomButtons(
                onCloseClick = onCloseClick,
                onSaveAndClose = onSaveAndClose,
            )
        }
    }
}
