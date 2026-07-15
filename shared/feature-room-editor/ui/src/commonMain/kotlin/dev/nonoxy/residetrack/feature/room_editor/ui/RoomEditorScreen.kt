package dev.nonoxy.residetrack.feature.room_editor.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Scaffold
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
import dev.nonoxy.residetrack.feature.room_editor.ui.views.EditorActionButtons
import dev.nonoxy.residetrack.feature.room_editor.ui.views.RoomEditorContent
import dev.nonoxy.residetrack.feature.room_editor.ui.views.DeleteRoomDialog
import dev.nonoxy.residetrack.feature.room_editor.ui.views.RemoveStudentDialog
import dev.nonoxy.residetrack.feature.room_editor.ui.views.RoomParamsSheet
import dev.nonoxy.residetrack.common.ui.common.dialog.DiscardChangesDialog
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackErrorSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.SnackbarType
import dev.nonoxy.residetrack.common.ui.common.utils.CollectFlow
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
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
            isLoading = state.isLoading,
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
            studentCount = state.initialStudentCount,
            isLoading = state.isLoading,
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ResideTrackTheme.colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            EditorActionButtons(
                onCloseClick = viewModel::onClose,
                onSaveAndClose = viewModel::onSaveAndClose,
                isSaveEnabled = state.isSaveEnabled,
                modifier = Modifier.imePadding(),
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    when (currentSnackbarType) {
                        SnackbarType.ERROR -> ResideTrackErrorSnackbar(snackbarData = snackbarData)
                        SnackbarType.INFO -> ResideTrackSnackbar(snackbarData = snackbarData)
                        else -> Unit
                    }
                },
            )
        },
    ) { innerPadding ->
        RoomEditorContent(
            modifier = Modifier.fillMaxSize(),
            bottomPadding = innerPadding.calculateBottomPadding(),
            state = state,
            onRetryClick = viewModel::onRetryLoadStudents,
            onEditRoomParamsClick = viewModel::onEditRoomParamsClick,
            onAddStudent = viewModel::onAddStudent,
            isAddStudentEnabled = state.isAddStudentEnabled,
            onRemoveStudent = viewModel::onRemoveStudent,
            onStreamNumberChange = viewModel::onStreamNumberChange,
            onCheckInDateChange = viewModel::onCheckInDateChange,
            onCheckOutDateChange = viewModel::onCheckOutDateChange,
            onCheckInDateMillisChange = viewModel::onCheckInDateMillisChange,
            onCheckOutDateMillisChange = viewModel::onCheckOutDateMillisChange,
            onOpenPicker = viewModel::onDatePickerOpen,
            onDismissPicker = viewModel::onDatePickerDismiss,
        )
    }
}
