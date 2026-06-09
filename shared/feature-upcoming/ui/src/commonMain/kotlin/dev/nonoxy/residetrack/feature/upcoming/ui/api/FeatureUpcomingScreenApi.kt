package dev.nonoxy.residetrack.feature.upcoming.ui.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.nonoxy.residetrack.core.navigation.Screen
import dev.nonoxy.residetrack.feature.upcoming.ui.UpcomingScreen
import kotlinx.serialization.Serializable

@Serializable
data object UpcomingRoute : Screen

fun NavGraphBuilder.composableUpcomingScreen(
    onNavigateToManageStudentsExistingRoom: (String) -> Unit,
) {
    composable<UpcomingRoute> {
        UpcomingScreen(onNavigateToManageStudentsExistingRoom = onNavigateToManageStudentsExistingRoom)
    }
}
