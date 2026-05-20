package dev.nonoxy.feature.manage_students.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nonoxy.core.design.common.state.ShowStateData
import dev.nonoxy.feature.manage_students.presentation.models.ManageStudentsEvent
import dev.nonoxy.feature.manage_students.presentation.models.ManageStudentsViewState

@Composable
internal fun ManageStudentsContent(
    state: ManageStudentsViewState,
    onObtainEvent: (ManageStudentsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    ShowStateData(
        modifier = modifier,
        state = state,
        isLoading = state.isLoading,
        isError = state.isError,
        onRetryClick = { onObtainEvent(ManageStudentsEvent.LoadStudents) }
    ) { currentState ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ManageStudentsTopBar(
                room = currentState.room,
                onCloseClick = { onObtainEvent(ManageStudentsEvent.OnClose) }
            )

            ManageStudentsList(
                students = currentState.editableStudents,
                onObtainEvent = onObtainEvent,
                modifier = Modifier.weight(1f)
            )

            ManageStudentsBottomButtons(
                onObtainEvent = onObtainEvent
            )
        }
    }
} 