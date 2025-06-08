package dev.nonoxy.feature.rooms.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nonoxy.feature.rooms.presentation.RoomsViewModel
import dev.nonoxy.feature.rooms.ui.views.RoomsScreenDetails
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun RoomsScreen(
    viewModel: RoomsViewModel = koinViewModel()
) {
    val viewState by viewModel.viewState().collectAsStateWithLifecycle()
    val viewAction by viewModel.viewAction().collectAsStateWithLifecycle(null)

    when (viewAction) {
        else -> {}
    }

    RoomsScreenDetails(
        state = viewState,
        onObtainEvent = viewModel::obtainEvent,
        modifier = Modifier.fillMaxSize()
    )
}
