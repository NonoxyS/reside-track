package dev.nonoxy.residetrack.feature.upcoming.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.nonoxy.residetrack.core.navigation.Screen
import dev.nonoxy.residetrack.core.navigation.navigateOnResumed
import dev.nonoxy.residetrack.feature.upcoming.ui.UpcomingScreen
import kotlinx.serialization.Serializable

@Serializable
data object UpcomingRoute : Screen

fun NavController.navigateToUpcomingScreen(
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(UpcomingRoute) {
        launchSingleTop = true
        popUpToScreen?.let { screen -> popUpTo(screen) { inclusive = popUpInclusive } }
    }
}

fun NavGraphBuilder.composableUpcomingScreen(
    onNavigateToManageStudentsExistingRoom: (String) -> Unit,
) {
    composable<UpcomingRoute> {
        UpcomingScreen(onNavigateToManageStudentsExistingRoom = onNavigateToManageStudentsExistingRoom)
    }
}
