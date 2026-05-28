package dev.nonoxy.residetrack.feature.add_room.ui.api

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import dev.nonoxy.residetrack.core.navigation.AddRoomRoute
import dev.nonoxy.residetrack.core.navigation.bottomsheet.ModalBottomSheetConfiguration
import dev.nonoxy.residetrack.core.navigation.bottomsheet.bottomSheet
import dev.nonoxy.residetrack.core.navigation.navigateOnResumed
import dev.nonoxy.residetrack.feature.add_room.ui.AddRoomScreen

fun NavController.navigateToAddRoomScreen() {
    navigateOnResumed(AddRoomRoute)
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.bottomSheetAddRoomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToManageStudentsDraftRoom: () -> Unit,
) {
    bottomSheet<AddRoomRoute>(
        configuration = ModalBottomSheetConfiguration(
            modifier = Modifier.statusBarsPadding().fillMaxWidth(),
        ),
    ) {
        AddRoomScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToManageStudentsDraftRoom = onNavigateToManageStudentsDraftRoom,
        )
    }
}
