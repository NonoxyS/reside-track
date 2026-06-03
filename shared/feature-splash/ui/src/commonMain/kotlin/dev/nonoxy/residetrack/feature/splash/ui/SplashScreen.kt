package dev.nonoxy.residetrack.feature.splash.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.nonoxy.residetrack.common.ui.common.utils.CollectFlow
import dev.nonoxy.residetrack.feature.splash.presentation.SplashScreenViewModel
import dev.nonoxy.residetrack.feature.splash.presentation.models.UiSplashLabel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SplashScreen(
    onNavigateToRooms: () -> Unit,
    viewModel: SplashScreenViewModel = koinViewModel(),
) {
    viewModel.label.CollectFlow { label ->
        when (label) {
            UiSplashLabel.NavigateToRooms -> onNavigateToRooms()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
