package dev.nonoxy.residetrack.feature.room_editor.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nonoxy.residetrack.feature.room_editor.presentation.RoomEditorViewModel
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiRoomEditorLabel
import dev.nonoxy.residetrack.feature.room_editor.ui.views.RoomEditorContent
import dev.nonoxy.residetrack.feature.room_editor.ui.views.DeleteRoomDialog
import dev.nonoxy.residetrack.feature.room_editor.ui.views.RemoveStudentDialog
import dev.nonoxy.residetrack.feature.room_editor.ui.views.RoomParamsSheet
import dev.nonoxy.residetrack.common.ui.common.dialog.DialogScaffold
import dev.nonoxy.residetrack.common.ui.common.dialog.DiscardChangesDialog
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackErrorSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.SnackbarType
import dev.nonoxy.residetrack.common.ui.common.utils.CollectFlow
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun RoomEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: RoomEditorViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by rememberSaveable { mutableStateOf(SnackbarType.INFO) }

    BackHandler(enabled = state.isDirty && !state.isLoading) {
        viewModel.onDismissRequested()
    }

    if (state.showDiscardConfirm) {
        DiscardChangesDialog(
            onConfirm = viewModel::onDiscardConfirmed,
            onDismiss = viewModel::onKeepEditing,
        )
    }

    if (state.showRoomParams && state.roomParams != null) {
        RoomParamsSheet(
            params = state.roomParams!!,
            onFloorChange = viewModel::onRoomParamsFloorChange,
            onRoomNumberChange = viewModel::onRoomParamsRoomNumberChange,
            onBedsChange = viewModel::onRoomParamsBedsChange,
            onSave = viewModel::onSaveRoomParams,
            onDelete = viewModel::onDeleteRoomClick,
            onDismiss = viewModel::onRoomParamsDismiss,
        )
    }

    if (state.removingStudentId != null) {
        RemoveStudentDialog(
            onConfirm = viewModel::onRemoveStudentConfirmed,
            onDismiss = viewModel::onRemoveStudentDismissed,
        )
    }

    if (state.showDeleteConfirm) {
        DeleteRoomDialog(
            roomNumber = state.room?.roomNumber.orEmpty(),
            studentCount = state.editableStudents.size,
            onConfirm = viewModel::onDeleteRoomConfirm,
            onDismiss = viewModel::onDeleteRoomDismiss,
        )
    }

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiRoomEditorLabel.NavigateBack -> onNavigateBack()
            is UiRoomEditorLabel.ShowError -> {
                currentSnackbarType = SnackbarType.ERROR
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = label.message,
                    withDismissAction = true,
                )
            }
            is UiRoomEditorLabel.ShowSuccess -> {
                currentSnackbarType = SnackbarType.INFO
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = label.message,
                    withDismissAction = true,
                )
            }
        }
    }

    DialogScaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    when (currentSnackbarType) {
                        SnackbarType.ERROR -> ResideTrackErrorSnackbar(snackbarData = snackbarData)
                        SnackbarType.INFO -> ResideTrackSnackbar(snackbarData = snackbarData)
                        else -> null
                    }
                },
            )
        },
    ) { paddingValues ->
        RoomEditorContent(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            state = state,
            onRetryClick = viewModel::onRetryLoadStudents,
            onCloseClick = viewModel::onClose,
            onEditRoomParamsClick = viewModel::onEditRoomParamsClick,
            onAddStudent = viewModel::onAddStudent,
            onRemoveStudent = viewModel::onRemoveStudent,
            onStreamNumberChange = viewModel::onStreamNumberChange,
            onCheckInDateChange = viewModel::onCheckInDateChange,
            onCheckOutDateChange = viewModel::onCheckOutDateChange,
            onCheckInDateMillisChange = viewModel::onCheckInDateMillisChange,
            onCheckOutDateMillisChange = viewModel::onCheckOutDateMillisChange,
            onOpenPicker = viewModel::onDatePickerOpen,
            onDismissPicker = viewModel::onDatePickerDismiss,
            onSaveAndClose = viewModel::onSaveAndClose,
        )
    }
}
