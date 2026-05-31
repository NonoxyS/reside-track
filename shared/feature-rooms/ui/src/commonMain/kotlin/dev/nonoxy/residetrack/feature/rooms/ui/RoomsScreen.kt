package dev.nonoxy.residetrack.feature.rooms.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nonoxy.residetrack.feature.rooms.presentation.RoomsViewModel
import dev.nonoxy.residetrack.feature.rooms.presentation.models.UiRoomsLabel
import dev.nonoxy.residetrack.feature.rooms.ui.views.RoomsScreenDetails
import dev.nonoxy.residetrack.common.ui.common.utils.CollectFlow
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun RoomsScreen(
    onNavigateToAddRoomScreen: () -> Unit,
    onNavigateToManageStudentsExistingRoom: (String) -> Unit,
    viewModel: RoomsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiRoomsLabel.NavigateToAddRoomScreen -> onNavigateToAddRoomScreen()
            is UiRoomsLabel.NavigateToManageStudentsExistingRoom ->
                onNavigateToManageStudentsExistingRoom(label.roomId)
        }
    }

    RoomsScreenDetails(
        state = state,
        onRoomClick = viewModel::onRoomClick,
        onAddRoomClick = viewModel::onAddRoomClick,
        modifier = Modifier.fillMaxSize()
    )
}
