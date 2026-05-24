package dev.nonoxy.residetrack.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.nonoxy.core.navigation.RoomsRoute
import dev.nonoxy.core.navigation.bottomsheet.ModalBottomSheetLayout
import dev.nonoxy.core.navigation.bottomsheet.rememberModalBottomSheetNavigator
import dev.nonoxy.feature.add_room.presentation.navigation.bottomSheetAddRoomScreen
import dev.nonoxy.feature.add_room.presentation.navigation.navigateToAddRoomScreen
import dev.nonoxy.feature.manage_students.presentation.navigation.bottomSheetManageStudentsExistingRoom
import dev.nonoxy.feature.manage_students.presentation.navigation.bottomSheetManageStudentsDraftRoom
import dev.nonoxy.feature.manage_students.presentation.navigation.navigateToManageStudentsExistingRoom
import dev.nonoxy.feature.manage_students.presentation.navigation.navigateToManageStudentsDraftRoom
import dev.nonoxy.feature.rooms.ui.api.composableRoomsScreen

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
            startDestination = RoomsRoute
        ) {
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
