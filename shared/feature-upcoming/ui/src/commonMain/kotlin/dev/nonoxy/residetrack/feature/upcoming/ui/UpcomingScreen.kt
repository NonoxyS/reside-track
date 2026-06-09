package dev.nonoxy.residetrack.feature.upcoming.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nonoxy.residetrack.common.ui.common.utils.CollectFlow
import dev.nonoxy.residetrack.feature.upcoming.presentation.UpcomingViewModel
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingLabel
import dev.nonoxy.residetrack.feature.upcoming.ui.views.UpcomingScreenDetails
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun UpcomingScreen(
    onNavigateToManageStudentsExistingRoom: (String) -> Unit,
    viewModel: UpcomingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            is UiUpcomingLabel.NavigateToManageStudentsExistingRoom ->
                onNavigateToManageStudentsExistingRoom(label.roomId)
        }
    }

    UpcomingScreenDetails(
        state = state,
        onItemClick = viewModel::onStudentClick,
        onRetryClick = viewModel::onRetryClick,
        modifier = Modifier.fillMaxSize(),
    )
}
