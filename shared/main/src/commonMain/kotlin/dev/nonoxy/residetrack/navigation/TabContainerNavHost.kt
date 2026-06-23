package dev.nonoxy.residetrack.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
    onNavigateToRoomEditorExistingRoom: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Tabs are sibling destinations — switching between them is instant.
    // Depth navigation (add room, room editor) lives in the outer
    // ResideTrackNavHost and keeps its transitions.
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = RoomsRoute,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composableRoomsScreen(
            onNavigateToAddRoomScreen = onNavigateToAddRoomScreen,
            onNavigateToRoomEditorExistingRoom = onNavigateToRoomEditorExistingRoom,
        )
        composableUpcomingScreen(
            onNavigateToRoomEditorExistingRoom = onNavigateToRoomEditorExistingRoom,
        )
    }
}
