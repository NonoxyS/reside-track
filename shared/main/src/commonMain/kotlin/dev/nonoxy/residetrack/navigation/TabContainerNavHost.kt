package dev.nonoxy.residetrack.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import dev.nonoxy.residetrack.feature.rooms.ui.api.RoomsRoute
import dev.nonoxy.residetrack.feature.rooms.ui.api.composableRoomsScreen
import dev.nonoxy.residetrack.feature.upcoming.ui.api.composableUpcomingScreen

@Composable
internal fun TabContainerNavHost(
    navController: NavHostController,
    onNavigateToAddRoomScreen: () -> Unit,
    onNavigateToManageStudentsExistingRoom: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = RoomsRoute,
    ) {
        composableRoomsScreen(
            onNavigateToAddRoomScreen = onNavigateToAddRoomScreen,
            onNavigateToManageStudentsExistingRoom = onNavigateToManageStudentsExistingRoom,
        )
        composableUpcomingScreen(
            onNavigateToManageStudentsExistingRoom = onNavigateToManageStudentsExistingRoom,
        )
    }
}
