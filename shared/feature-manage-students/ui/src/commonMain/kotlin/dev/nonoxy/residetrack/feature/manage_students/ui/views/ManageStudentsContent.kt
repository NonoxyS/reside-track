package dev.nonoxy.residetrack.feature.manage_students.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiDateField
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiManageStudentsState
import dev.nonoxy.residetrack.common.ui.common.state.ShowStateData

@Composable
internal fun ManageStudentsContent(
    state: UiManageStudentsState,
    onRetryClick: () -> Unit,
    onCloseClick: () -> Unit,
    onEditRoomParamsClick: () -> Unit,
    onAddStudent: () -> Unit,
    onRemoveStudent: (String) -> Unit,
    onStreamNumberChange: (String, String) -> Unit,
    onCheckInDateChange: (String, String) -> Unit,
    onCheckOutDateChange: (String, String) -> Unit,
    onCheckInDateMillisChange: (String, Long) -> Unit,
    onCheckOutDateMillisChange: (String, Long) -> Unit,
    onOpenPicker: (String, UiDateField) -> Unit,
    onDismissPicker: () -> Unit,
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

            val room = currentState.room
            if (room != null) {
                RoomSummaryHeader(
                    room = room,
                    onEditClick = onEditRoomParamsClick,
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                ManageStudentsList(
                    students = currentState.editableStudents,
                    openDatePicker = currentState.openDatePicker,
                    onAddStudent = onAddStudent,
                    onRemoveStudent = onRemoveStudent,
                    onStreamNumberChange = onStreamNumberChange,
                    onCheckInDateChange = onCheckInDateChange,
                    onCheckOutDateChange = onCheckOutDateChange,
                    onCheckInDateMillisChange = onCheckInDateMillisChange,
                    onCheckOutDateMillisChange = onCheckOutDateMillisChange,
                    onOpenPicker = onOpenPicker,
                    onDismissPicker = onDismissPicker,
                    modifier = Modifier.fillMaxSize(),
                )

                EditorActionButtons(
                    onCloseClick = onCloseClick,
                    onSaveAndClose = onSaveAndClose,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}
