package dev.nonoxy.residetrack.feature.splash.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.notifications.RemoteNotificationPermission
import dev.nonoxy.residetrack.common.ui.common.loader.ResideTrackLoader
import dev.nonoxy.residetrack.common.ui.common.utils.CollectFlow
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.feature.splash.presentation.SplashScreenViewModel
import dev.nonoxy.residetrack.feature.splash.presentation.models.UiSplashLabel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SplashScreen(
    onNavigateToRooms: () -> Unit,
    viewModel: SplashScreenViewModel = koinViewModel(),
) {
    val permissionsFactory = rememberPermissionsControllerFactory()
    val permissionsController = remember(permissionsFactory) {
        permissionsFactory.createPermissionsController()
    }
    BindEffect(permissionsController)

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiSplashLabel.NavigateToRooms -> {
                runCatching { permissionsController.providePermission(RemoteNotificationPermission) }
                onNavigateToRooms()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ResideTrackTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        ResideTrackLoader()
    }
}
