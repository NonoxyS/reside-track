package dev.nonoxy.feature.manage_students.ui

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
import dev.nonoxy.core.design.common.dialog.DialogScaffold
import dev.nonoxy.core.design.common.snackbar.ResideTrackErrorSnackbar
import dev.nonoxy.core.design.common.snackbar.ResideTrackSnackbar
import dev.nonoxy.core.design.common.snackbar.SnackbarType
import dev.nonoxy.core.design.common.utils.CollectFlow
import dev.nonoxy.feature.manage_students.presentation.ManageStudentsViewModel
import dev.nonoxy.feature.manage_students.presentation.models.ManageStudentsAction
import dev.nonoxy.feature.manage_students.ui.views.ManageStudentsContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ManageStudentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageStudentsViewModel = koinViewModel()
) {
    val viewState by viewModel.viewState().collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by rememberSaveable { mutableStateOf(SnackbarType.INFO) }

    viewModel.viewAction().CollectFlow { viewAction ->
        when (val currentAction = viewAction) {
            ManageStudentsAction.NavigateBack -> {
                onNavigateBack()
            }
            is ManageStudentsAction.ShowError -> {
                currentSnackbarType = SnackbarType.ERROR
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = currentAction.message,
                    withDismissAction = true
                )
            }
            is ManageStudentsAction.ShowSuccess -> {
                currentSnackbarType = SnackbarType.INFO
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = currentAction.message,
                    withDismissAction = true
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
                }
            )
        }
    ) { paddingValues ->
        ManageStudentsContent(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            state = viewState,
            onObtainEvent = viewModel::obtainEvent
        )
    }
}
