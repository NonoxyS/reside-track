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
import dev.nonoxy.feature.add_room.presentation.AddRoomViewModel
import dev.nonoxy.feature.add_room.presentation.models.UiAddRoomLabel
import dev.nonoxy.feature.add_room.ui.views.AddRoomScreenContent
import dev.nonoxy.residetrack.common.resources.StringConverter
import dev.nonoxy.residetrack.common.ui.common.dialog.DialogScaffold
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackErrorSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.SnackbarType
import dev.nonoxy.residetrack.common.ui.common.utils.CollectFlow
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AddRoomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToManageStudentsDraftRoom: () -> Unit,
    viewModel: AddRoomViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stringConverter: StringConverter = koinInject()

    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by rememberSaveable { mutableStateOf(SnackbarType.INFO) }

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiAddRoomLabel.CloseScreen -> onNavigateBack()
            UiAddRoomLabel.NavigateToManageStudentsDraftRoom -> onNavigateToManageStudentsDraftRoom()
            is UiAddRoomLabel.ShowSuccess -> {
                currentSnackbarType = SnackbarType.INFO
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = label.kind.localized(stringConverter),
                    withDismissAction = true,
                )
            }
            is UiAddRoomLabel.ShowError -> {
                currentSnackbarType = SnackbarType.ERROR
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = label.kind.localized(stringConverter),
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
        AddRoomScreenContent(
            modifier = Modifier.fillMaxWidth().padding(paddingValues),
            state = state,
            onFloorNumberInputValueChange = viewModel::onFloorNumberInputValueChange,
            onFloorNumberSelect = viewModel::onFloorNumberSelect,
            onRoomNumberInputValueChange = viewModel::onRoomNumberInputValueChange,
            onBedsCountInputValueChange = viewModel::onBedsCountInputValueChange,
            onBedsCountSelect = viewModel::onBedsCountSelect,
            onCreateRoomClick = viewModel::onCreateRoomClick,
            onCancelClick = viewModel::onCancelClick,
            onToggleFloorInput = viewModel::onToggleFloorInput,
            onToggleBedsInput = viewModel::onToggleBedsInput,
        )
    }
}
