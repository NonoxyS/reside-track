package dev.nonoxy.residetrack.feature.manage_students.ui.api

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import dev.nonoxy.residetrack.core.navigation.ManageStudentsDraftRoomRoute
import dev.nonoxy.residetrack.core.navigation.ManageStudentsExistingRoomRoute
import dev.nonoxy.residetrack.core.navigation.bottomsheet.ModalBottomSheetConfiguration
import dev.nonoxy.residetrack.core.navigation.bottomsheet.bottomSheet
import dev.nonoxy.residetrack.core.navigation.navigateOnResumed
import dev.nonoxy.residetrack.feature.manage_students.api.models.ManageStudentsMode
import dev.nonoxy.residetrack.feature.manage_students.ui.ManageStudentsScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToManageStudentsExistingRoom(roomId: String) {
    navigateOnResumed(ManageStudentsExistingRoomRoute(roomId))
}

fun NavController.navigateToManageStudentsDraftRoom() {
    navigateOnResumed(ManageStudentsDraftRoomRoute)
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.bottomSheetManageStudentsExistingRoom(
    onNavigateBack: () -> Unit,
) {
    bottomSheet<ManageStudentsExistingRoomRoute>(
        configuration = ModalBottomSheetConfiguration(
            modifier = Modifier.statusBarsPadding().fillMaxWidth(),
        ),
    ) { backStackEntry ->
        val roomId = backStackEntry.toRoute<ManageStudentsExistingRoomRoute>().roomId
        ManageStudentsScreen(
            onNavigateBack = onNavigateBack,
            viewModel = koinViewModel { parametersOf(ManageStudentsMode.ExistingRoom(roomId)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.bottomSheetManageStudentsDraftRoom(
    onNavigateBack: () -> Unit,
) {
    bottomSheet<ManageStudentsDraftRoomRoute>(
        configuration = ModalBottomSheetConfiguration(
            modifier = Modifier.statusBarsPadding().fillMaxWidth(),
        ),
    ) {
        ManageStudentsScreen(
            onNavigateBack = onNavigateBack,
            viewModel = koinViewModel { parametersOf(ManageStudentsMode.DraftRoom) },
        )
    }
}
