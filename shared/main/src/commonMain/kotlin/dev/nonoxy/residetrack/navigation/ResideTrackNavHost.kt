package dev.nonoxy.residetrack.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.nonoxy.residetrack.core.navigation.bottomsheet.ModalBottomSheetLayout
import dev.nonoxy.residetrack.core.navigation.bottomsheet.rememberModalBottomSheetNavigator
import dev.nonoxy.residetrack.feature.splash.ui.api.SplashRoute
import dev.nonoxy.residetrack.feature.splash.ui.api.composableSplashScreen
import dev.nonoxy.residetrack.feature.rooms.ui.api.navigateToRoomsScreen
import dev.nonoxy.residetrack.feature.add_room.ui.api.bottomSheetAddRoomScreen
import dev.nonoxy.residetrack.feature.add_room.ui.api.navigateToAddRoomScreen
import dev.nonoxy.residetrack.feature.manage_students.ui.api.bottomSheetManageStudentsExistingRoom
import dev.nonoxy.residetrack.feature.manage_students.ui.api.bottomSheetManageStudentsDraftRoom
import dev.nonoxy.residetrack.feature.manage_students.ui.api.navigateToManageStudentsExistingRoom
import dev.nonoxy.residetrack.feature.manage_students.ui.api.navigateToManageStudentsDraftRoom
import dev.nonoxy.residetrack.feature.rooms.ui.api.composableRoomsScreen

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
                    navController.navigateToRoomsScreen(popUpToScreen = SplashRoute)
                }
            )

            composableRoomsScreen(
                onNavigateToAddRoomScreen = navController::navigateToAddRoomScreen,
                onNavigateToManageStudentsExistingRoom = navController::navigateToManageStudentsExistingRoom
            )

            bottomSheetAddRoomScreen(
                onNavigateBack = navController::popBackStack,
                onNavigateToManageStudentsDraftRoom = navController::navigateToManageStudentsDraftRoom
            )

            bottomSheetManageStudentsExistingRoom(
                onNavigateBack = navController::popBackStack
            )

            bottomSheetManageStudentsDraftRoom(
                onNavigateBack = navController::popBackStack
            )
        }
    }
}
