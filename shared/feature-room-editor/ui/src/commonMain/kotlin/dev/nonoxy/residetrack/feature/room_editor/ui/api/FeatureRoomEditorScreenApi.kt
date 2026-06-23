package dev.nonoxy.residetrack.feature.room_editor.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.nonoxy.residetrack.core.navigation.Screen
import dev.nonoxy.residetrack.core.navigation.navigateOnResumed
import dev.nonoxy.residetrack.feature.room_editor.api.models.RoomEditorMode
import dev.nonoxy.residetrack.feature.room_editor.ui.RoomEditorScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class RoomEditorExistingRoomRoute(val roomId: String) : Screen

@Serializable
data object RoomEditorDraftRoomRoute : Screen

fun NavController.navigateToRoomEditorExistingRoom(
    roomId: String,
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(RoomEditorExistingRoomRoute(roomId)) {
        launchSingleTop = true
        popUpToScreen?.let { screen ->
            popUpTo(screen) { inclusive = popUpInclusive }
        }
    }
}

fun NavGraphBuilder.composableRoomEditorExistingRoom(
    onNavigateBack: () -> Unit,
) {
    composable<RoomEditorExistingRoomRoute> { backStackEntry ->
        val roomId = backStackEntry.toRoute<RoomEditorExistingRoomRoute>().roomId
        RoomEditorScreen(
            onNavigateBack = onNavigateBack,
            viewModel = koinViewModel { parametersOf(RoomEditorMode.ExistingRoom(roomId)) },
        )
    }
}

fun NavGraphBuilder.composableRoomEditorDraftRoom(
    onNavigateBack: () -> Unit,
) {
    composable<RoomEditorDraftRoomRoute> {
        RoomEditorScreen(
            onNavigateBack = onNavigateBack,
            viewModel = koinViewModel { parametersOf(RoomEditorMode.DraftRoom) },
        )
    }
}
