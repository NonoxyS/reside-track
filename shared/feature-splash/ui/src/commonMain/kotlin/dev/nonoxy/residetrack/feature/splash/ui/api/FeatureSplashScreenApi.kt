package dev.nonoxy.residetrack.feature.splash.ui.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.nonoxy.residetrack.core.navigation.Screen
import dev.nonoxy.residetrack.feature.splash.ui.SplashScreen
import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute : Screen

fun NavGraphBuilder.composableSplashScreen(
    onNavigateToRooms: () -> Unit,
) {
    composable<SplashRoute> {
        SplashScreen(onNavigateToRooms = onNavigateToRooms)
    }
}
