package dev.nonoxy.feature.add_room.presentation.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import dev.nonoxy.core.navigation.Screen
import dev.nonoxy.core.navigation.bottom_sheet.ModalBottomSheetConfiguration
import dev.nonoxy.core.navigation.bottom_sheet.bottomSheet
import dev.nonoxy.feature.add_room.ui.AddRoomScreen

fun NavController.navigateToAddRoomScreen() {
    navigate(Screen.AddRoom) {
        launchSingleTop = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.bottomSheetAddRoomScreen(
    onNavigateBack: () -> Unit,
) {
    bottomSheet<Screen.AddRoom>(
        configuration = ModalBottomSheetConfiguration(
            modifier = Modifier.statusBarsPadding().fillMaxWidth()
        )
    ) { backStackEntry ->
        AddRoomScreen(
            onNavigateBack = onNavigateBack,
        )
    }
}
