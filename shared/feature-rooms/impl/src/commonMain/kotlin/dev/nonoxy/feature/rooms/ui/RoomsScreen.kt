package dev.nonoxy.feature.rooms.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nonoxy.core.design.common.utils.CollectFlow
import dev.nonoxy.feature.rooms.presentation.RoomsViewModel
import dev.nonoxy.feature.rooms.presentation.models.RoomsAction
import dev.nonoxy.feature.rooms.ui.views.RoomsScreenDetails
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun RoomsScreen(
    onNavigateToAddRoomScreen: () -> Unit,
    viewModel: RoomsViewModel = koinViewModel()
) {
    val viewState by viewModel.viewState().collectAsStateWithLifecycle()

    viewModel.viewAction().CollectFlow { viewAction ->
        when (viewAction) {
            RoomsAction.NavigateToAddRoomScreen -> onNavigateToAddRoomScreen()
        }
    }

    RoomsScreenDetails(
        state = viewState,
        onObtainEvent = viewModel::obtainEvent,
        modifier = Modifier.fillMaxSize()
    )
}
