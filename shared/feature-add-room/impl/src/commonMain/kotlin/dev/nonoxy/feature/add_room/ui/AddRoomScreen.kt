package dev.nonoxy.feature.add_room.ui

import androidx.compose.foundation.layout.fillMaxWidth
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
import dev.nonoxy.residetrack.common.ui.common.dialog.DialogScaffold
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackErrorSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.SnackbarType
import dev.nonoxy.residetrack.common.ui.common.utils.CollectFlow
import dev.nonoxy.feature.add_room.presentation.OldAddRoomViewModel
import dev.nonoxy.feature.add_room.presentation.models.AddRoomAction
import dev.nonoxy.feature.add_room.ui.views.AddRoomScreenContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AddRoomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToManageStudentsDraftRoom: () -> Unit,
    viewModel: OldAddRoomViewModel = koinViewModel()
) {
    val viewState by viewModel.viewState().collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by rememberSaveable { mutableStateOf(SnackbarType.INFO) }

    viewModel.viewAction().CollectFlow { viewAction ->
        when (val currentAction = viewAction) {
            is AddRoomAction.CloseScreen -> {
                onNavigateBack()
            }

            is AddRoomAction.ShowSuccessMessage -> {
                currentSnackbarType = SnackbarType.INFO
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = currentAction.message,
                    withDismissAction = true
                )
            }

            is AddRoomAction.ShowErrorMessage -> {
                currentSnackbarType = SnackbarType.ERROR
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = currentAction.message,
                    withDismissAction = true
                )
            }

            AddRoomAction.NavigateToManageStudentsDraftRoom -> {
                onNavigateToManageStudentsDraftRoom()
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
                }
            )
        }
    ) { paddingValues ->
        AddRoomScreenContent(
            modifier = Modifier.fillMaxWidth().padding(paddingValues),
            state = viewState,
            onObtainEvent = viewModel::obtainEvent
        )
    }
}
