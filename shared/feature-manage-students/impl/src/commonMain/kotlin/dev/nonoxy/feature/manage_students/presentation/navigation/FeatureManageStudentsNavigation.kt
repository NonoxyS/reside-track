package dev.nonoxy.feature.manage_students.presentation.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import dev.nonoxy.core.navigation.Screen
import dev.nonoxy.core.navigation.bottom_sheet.ModalBottomSheetConfiguration
import dev.nonoxy.core.navigation.bottom_sheet.bottomSheet
import dev.nonoxy.feature.manage_students.models.ManageStudentsMode
import dev.nonoxy.feature.manage_students.ui.ManageStudentsScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToManageStudentsExistingRoom(roomId: String) {
    navigate(Screen.ManageStudentsExistingRoom(roomId)) {
        launchSingleTop = true
    }
}

fun NavController.navigateToManageStudentsDraftRoom() {
    navigate(Screen.ManageStudentsDraftRoom) {
        launchSingleTop = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.bottomSheetManageStudentsExistingRoom(
    onNavigateBack: () -> Unit,
) {
    bottomSheet<Screen.ManageStudentsExistingRoom>(
        configuration = ModalBottomSheetConfiguration(
            modifier = Modifier.statusBarsPadding().fillMaxWidth()
        )
    ) { backStackEntry ->
        val roomId = backStackEntry.toRoute<Screen.ManageStudentsExistingRoom>().roomId

        ManageStudentsScreen(
            onNavigateBack = onNavigateBack,
            viewModel = koinViewModel { parametersOf(ManageStudentsMode.ExistingRoom(roomId)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.bottomSheetManageStudentsDraftRoom(
    onNavigateBack: () -> Unit,
) {
    bottomSheet<Screen.ManageStudentsDraftRoom>(
        configuration = ModalBottomSheetConfiguration(
            modifier = Modifier.statusBarsPadding().fillMaxWidth()
        )
    ) { backStackEntry ->
        ManageStudentsScreen(
            onNavigateBack = onNavigateBack,
            viewModel = koinViewModel { parametersOf(ManageStudentsMode.DraftRoom) }
        )
    }
}