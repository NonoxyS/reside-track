package dev.nonoxy.residetrack.feature.manage_students.ui

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
import dev.nonoxy.residetrack.feature.manage_students.presentation.ManageStudentsViewModel
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiManageStudentsLabel
import dev.nonoxy.residetrack.feature.manage_students.ui.views.ManageStudentsContent
import dev.nonoxy.residetrack.common.resources.StringConverter
import dev.nonoxy.residetrack.common.ui.common.dialog.DialogScaffold
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackErrorSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.SnackbarType
import dev.nonoxy.residetrack.common.ui.common.utils.CollectFlow
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ManageStudentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageStudentsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stringConverter: StringConverter = koinInject()

    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by rememberSaveable { mutableStateOf(SnackbarType.INFO) }

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiManageStudentsLabel.NavigateBack -> onNavigateBack()
            is UiManageStudentsLabel.ShowError -> {
                currentSnackbarType = SnackbarType.ERROR
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = label.kind.localized(stringConverter),
                    withDismissAction = true,
                )
            }
            is UiManageStudentsLabel.ShowSuccess -> {
                currentSnackbarType = SnackbarType.INFO
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
        ManageStudentsContent(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            state = state,
            onRetryClick = viewModel::onRetryLoadStudents,
            onCloseClick = viewModel::onClose,
            onAddStudent = viewModel::onAddStudent,
            onRemoveStudent = viewModel::onRemoveStudent,
            onStreamNumberChange = viewModel::onStreamNumberChange,
            onCheckInDateChange = viewModel::onCheckInDateChange,
            onCheckOutDateChange = viewModel::onCheckOutDateChange,
            onCheckInDateMillisChange = viewModel::onCheckInDateMillisChange,
            onCheckOutDateMillisChange = viewModel::onCheckOutDateMillisChange,
            onSaveAndClose = viewModel::onSaveAndClose,
        )
    }
}
