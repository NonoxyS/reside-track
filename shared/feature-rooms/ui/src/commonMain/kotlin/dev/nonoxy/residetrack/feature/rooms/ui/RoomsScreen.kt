package dev.nonoxy.residetrack.feature.rooms.ui

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nonoxy.residetrack.core.navigation.bottombar.LocalFloatingBottomBarInset
import dev.nonoxy.residetrack.feature.rooms.presentation.RoomsViewModel
import dev.nonoxy.residetrack.feature.rooms.presentation.models.UiRoomsLabel
import dev.nonoxy.residetrack.feature.rooms.ui.views.ImportConfirmDialog
import dev.nonoxy.residetrack.feature.rooms.ui.views.RoomsScreenDetails
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackErrorSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackSuccessSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.SnackbarType
import dev.nonoxy.residetrack.common.ui.common.utils.CollectFlow
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun RoomsScreen(
    onNavigateToAddRoomScreen: () -> Unit,
    onNavigateToRoomEditorExistingRoom: (String) -> Unit,
    viewModel: RoomsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarType by rememberSaveable { mutableStateOf(SnackbarType.INFO) }

    val filePicker = rememberBackupFilePicker(
        onImported = viewModel::onBackupFileLoaded,
        onExportCompleted = viewModel::onExportCompleted,
    )

    state.importConfirmation?.let { confirmation ->
        ImportConfirmDialog(
            roomCount = confirmation.roomCount,
            studentCount = confirmation.studentCount,
            currentRoomCount = confirmation.currentRoomCount,
            currentStudentCount = confirmation.currentStudentCount,
            onConfirm = viewModel::onRestoreConfirm,
            onDismiss = viewModel::onRestoreCancel,
        )
    }

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiRoomsLabel.NavigateToAddRoomScreen -> onNavigateToAddRoomScreen()
            is UiRoomsLabel.NavigateToRoomEditorExistingRoom ->
                onNavigateToRoomEditorExistingRoom(label.roomId)

            is UiRoomsLabel.SaveBackupFile ->
                filePicker.launchSave(json = label.json, suggestedName = label.suggestedName)

            UiRoomsLabel.OpenBackupFile -> filePicker.launchOpen()

            is UiRoomsLabel.ShowBackupSuccess -> {
                snackbarType = SnackbarType.SUCCESS
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(message = label.message, withDismissAction = true)
            }

            is UiRoomsLabel.ShowBackupError -> {
                snackbarType = SnackbarType.ERROR
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(message = label.message, withDismissAction = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RoomsScreenDetails(
            state = state,
            onRoomClick = viewModel::onRoomClick,
            onAddRoomClick = viewModel::onAddRoomClick,
            onRetryClick = viewModel::onRetryClick,
            onBackupExportClick = viewModel::onExportClick,
            onBackupImportClick = viewModel::onImportClick,
            modifier = Modifier.fillMaxSize(),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = LocalFloatingBottomBarInset.current),
            snackbar = { snackbarData ->
                when (snackbarType) {
                    SnackbarType.SUCCESS -> ResideTrackSuccessSnackbar(snackbarData = snackbarData)
                    SnackbarType.ERROR -> ResideTrackErrorSnackbar(snackbarData = snackbarData)
                    else -> ResideTrackSnackbar(snackbarData = snackbarData)
                }
            },
        )
    }
}
