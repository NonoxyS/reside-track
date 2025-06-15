package dev.nonoxy.residetrack.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.nonoxy.core.navigation.Screen
import dev.nonoxy.core.navigation.bottom_sheet.ModalBottomSheetLayout
import dev.nonoxy.core.navigation.bottom_sheet.rememberModalBottomSheetNavigator
import dev.nonoxy.feature.rooms.presentation.navigation.composableRoomsScreen

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
            startDestination = Screen.Rooms
        ) {
            composableRoomsScreen()
        }
    }
}
