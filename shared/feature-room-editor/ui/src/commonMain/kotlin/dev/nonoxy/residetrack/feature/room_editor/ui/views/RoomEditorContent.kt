package dev.nonoxy.residetrack.feature.room_editor.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiDateField
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiRoomEditorState
import dev.nonoxy.residetrack.common.ui.common.state.ShowStateData
import dev.nonoxy.residetrack.common.ui.theme.padding_size_8

@Composable
internal fun RoomEditorContent(
    state: UiRoomEditorState,
    onRetryClick: () -> Unit,
    onEditRoomParamsClick: () -> Unit,
    onAddStudent: () -> Unit,
    isAddStudentEnabled: Boolean,
    onRemoveStudent: (String) -> Unit,
    onStreamNumberChange: (String, String) -> Unit,
    onCheckInDateChange: (String, String) -> Unit,
    onCheckOutDateChange: (String, String) -> Unit,
    onCheckInDateMillisChange: (String, Long) -> Unit,
    onCheckOutDateMillisChange: (String, Long) -> Unit,
    onOpenPicker: (String, UiDateField) -> Unit,
    onDismissPicker: () -> Unit,
    bottomPadding: Dp = 0.dp,
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
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
        ) {
            val room = currentState.room
            if (room != null) {
                RoomSummaryHeader(
                    room = room,
                    onEditClick = onEditRoomParamsClick,
                    modifier = Modifier.padding(top = padding_size_8),
                )
            }

            RoomEditorList(
                students = currentState.editableStudents,
                openDatePicker = currentState.openDatePicker,
                onAddStudent = onAddStudent,
                isAddStudentEnabled = isAddStudentEnabled,
                bottomPadding = bottomPadding,
                onRemoveStudent = onRemoveStudent,
                onStreamNumberChange = onStreamNumberChange,
                onCheckInDateChange = onCheckInDateChange,
                onCheckOutDateChange = onCheckOutDateChange,
                onCheckInDateMillisChange = onCheckInDateMillisChange,
                onCheckOutDateMillisChange = onCheckOutDateMillisChange,
                onOpenPicker = onOpenPicker,
                onDismissPicker = onDismissPicker,
                modifier = Modifier.weight(1f).fillMaxSize(),
            )
        }
    }
}
