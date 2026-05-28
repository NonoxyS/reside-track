package dev.nonoxy.residetrack.feature.rooms.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.nonoxy.residetrack.core.navigation.RoomsRoute
import dev.nonoxy.residetrack.core.navigation.navigateOnResumed
import dev.nonoxy.residetrack.feature.rooms.ui.RoomsScreen

fun NavController.navigateToRoomsScreen() {
    navigateOnResumed(RoomsRoute)
}

fun NavGraphBuilder.composableRoomsScreen(
    onNavigateToAddRoomScreen: () -> Unit,
    onNavigateToManageStudentsExistingRoom: (String) -> Unit
) {
    composable<RoomsRoute> {
        RoomsScreen(
            onNavigateToAddRoomScreen = onNavigateToAddRoomScreen,
            onNavigateToManageStudentsExistingRoom = onNavigateToManageStudentsExistingRoom
        )
    }
}
