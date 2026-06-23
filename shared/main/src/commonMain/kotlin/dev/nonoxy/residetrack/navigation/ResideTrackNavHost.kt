package dev.nonoxy.residetrack.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.nonoxy.residetrack.core.navigation.bottomsheet.ModalBottomSheetLayout
import dev.nonoxy.residetrack.core.navigation.bottomsheet.rememberModalBottomSheetNavigator
import dev.nonoxy.residetrack.feature.splash.ui.api.SplashRoute
import dev.nonoxy.residetrack.feature.splash.ui.api.composableSplashScreen
import dev.nonoxy.residetrack.feature.add_room.ui.api.AddRoomRoute
import dev.nonoxy.residetrack.feature.add_room.ui.api.bottomSheetAddRoomScreen
import dev.nonoxy.residetrack.feature.add_room.ui.api.navigateToAddRoomScreen
import dev.nonoxy.residetrack.feature.room_editor.ui.api.RoomEditorDraftRoomRoute
import dev.nonoxy.residetrack.feature.room_editor.ui.api.composableRoomEditorExistingRoom
import dev.nonoxy.residetrack.feature.room_editor.ui.api.composableRoomEditorDraftRoom
import dev.nonoxy.residetrack.feature.room_editor.ui.api.navigateToRoomEditorExistingRoom
import dev.nonoxy.residetrack.ui.tabcontainer.TabContainerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ResideTrackNavHost(
    modifier: Modifier = Modifier
) {
    val bottomSheetNavigator = rememberModalBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)

    ModalBottomSheetLayout(modalBottomSheetNavigator = bottomSheetNavigator) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = SplashRoute
        ) {
            composableSplashScreen(
                onNavigateToRooms = {
                    navController.navigate(TabContainerRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )

            composable<TabContainerRoute> {
                TabContainerScreen(
                    onNavigateToAddRoomScreen = navController::navigateToAddRoomScreen,
                    onNavigateToRoomEditorExistingRoom = navController::navigateToRoomEditorExistingRoom,
                )
            }

            bottomSheetAddRoomScreen(
                onNavigateBack = navController::popBackStack,
                // Navigate directly (not navigateOnResumed): the add-room bottom sheet is a
                // FloatingWindow entry that never reaches RESUMED, so the resumed-guard would
                // silently drop this hand-off. popUpTo removes the sheet so Back returns to rooms.
                onNavigateToRoomEditorDraftRoom = {
                    navController.navigate(RoomEditorDraftRoomRoute) {
                        popUpTo(AddRoomRoute) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )

            composableRoomEditorExistingRoom(
                onNavigateBack = navController::popBackStack
            )

            composableRoomEditorDraftRoom(
                onNavigateBack = navController::popBackStack
            )
        }
    }
}
