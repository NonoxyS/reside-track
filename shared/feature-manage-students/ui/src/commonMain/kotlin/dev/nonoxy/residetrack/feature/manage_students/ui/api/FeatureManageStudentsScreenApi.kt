package dev.nonoxy.residetrack.feature.manage_students.ui.api

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import dev.nonoxy.residetrack.core.navigation.Screen
import dev.nonoxy.residetrack.core.navigation.bottomsheet.ModalBottomSheetConfiguration
import dev.nonoxy.residetrack.core.navigation.bottomsheet.bottomSheet
import dev.nonoxy.residetrack.core.navigation.navigateOnResumed
import dev.nonoxy.residetrack.feature.manage_students.api.models.ManageStudentsMode
import dev.nonoxy.residetrack.feature.manage_students.ui.ManageStudentsScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class ManageStudentsExistingRoomRoute(val roomId: String) : Screen

@Serializable
data object ManageStudentsDraftRoomRoute : Screen

fun NavController.navigateToManageStudentsExistingRoom(
    roomId: String,
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(ManageStudentsExistingRoomRoute(roomId)) {
        launchSingleTop = true
        popUpToScreen?.let { screen ->
            popUpTo(screen) { inclusive = popUpInclusive }
        }
    }
}

fun NavController.navigateToManageStudentsDraftRoom(
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(ManageStudentsDraftRoomRoute) {
        launchSingleTop = true
        popUpToScreen?.let { screen ->
            popUpTo(screen) { inclusive = popUpInclusive }
        }
    }
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
