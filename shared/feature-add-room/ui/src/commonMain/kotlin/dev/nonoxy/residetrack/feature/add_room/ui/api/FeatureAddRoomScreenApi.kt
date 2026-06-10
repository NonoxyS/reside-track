package dev.nonoxy.residetrack.feature.add_room.ui.api

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import dev.nonoxy.residetrack.core.navigation.Screen
import dev.nonoxy.residetrack.core.navigation.bottomsheet.ModalBottomSheetConfiguration
import dev.nonoxy.residetrack.core.navigation.bottomsheet.bottomSheet
import dev.nonoxy.residetrack.core.navigation.navigateOnResumed
import dev.nonoxy.residetrack.feature.add_room.ui.AddRoomScreen
import kotlinx.serialization.Serializable

@Serializable
data object AddRoomRoute : Screen

fun NavController.navigateToAddRoomScreen(
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(AddRoomRoute) {
        launchSingleTop = true
        popUpToScreen?.let { screen ->
            popUpTo(screen) { inclusive = popUpInclusive }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.bottomSheetAddRoomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRoomEditorDraftRoom: () -> Unit,
) {
    bottomSheet<AddRoomRoute>(
        configuration = ModalBottomSheetConfiguration(
            modifier = Modifier.statusBarsPadding().fillMaxWidth(),
        ),
    ) {
        AddRoomScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToRoomEditorDraftRoom = onNavigateToRoomEditorDraftRoom,
        )
    }
}
