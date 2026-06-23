package dev.nonoxy.residetrack.feature.rooms.ui.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.nonoxy.residetrack.core.navigation.Screen
import dev.nonoxy.residetrack.feature.rooms.ui.RoomsScreen
import kotlinx.serialization.Serializable

@Serializable
data object RoomsRoute : Screen

fun NavGraphBuilder.composableRoomsScreen(
    onNavigateToAddRoomScreen: () -> Unit,
    onNavigateToRoomEditorExistingRoom: (String) -> Unit
) {
    composable<RoomsRoute> {
        RoomsScreen(
            onNavigateToAddRoomScreen = onNavigateToAddRoomScreen,
            onNavigateToRoomEditorExistingRoom = onNavigateToRoomEditorExistingRoom
        )
    }
}
