package dev.nonoxy.feature.rooms.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.nonoxy.core.navigation.Screen
import dev.nonoxy.core.navigation.navigateOnResumed
import dev.nonoxy.feature.rooms.ui.RoomsScreen

fun NavController.navigateToRoomsScreen() {
    navigateOnResumed<Screen.Rooms>()
}

fun NavGraphBuilder.composableRoomsScreen(
    onNavigateToAddRoomScreen: () -> Unit
) {
    composable<Screen.Rooms> {
        RoomsScreen(
            onNavigateToAddRoomScreen = onNavigateToAddRoomScreen
        )
    }
}
