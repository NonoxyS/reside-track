package dev.nonoxy.residetrack.feature.manage_students.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.nonoxy.residetrack.core.navigation.Screen
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

fun NavGraphBuilder.composableManageStudentsExistingRoom(
    onNavigateBack: () -> Unit,
) {
    composable<ManageStudentsExistingRoomRoute> { backStackEntry ->
        val roomId = backStackEntry.toRoute<ManageStudentsExistingRoomRoute>().roomId
        ManageStudentsScreen(
            onNavigateBack = onNavigateBack,
            viewModel = koinViewModel { parametersOf(ManageStudentsMode.ExistingRoom(roomId)) },
        )
    }
}

fun NavGraphBuilder.composableManageStudentsDraftRoom(
    onNavigateBack: () -> Unit,
) {
    composable<ManageStudentsDraftRoomRoute> {
        ManageStudentsScreen(
            onNavigateBack = onNavigateBack,
            viewModel = koinViewModel { parametersOf(ManageStudentsMode.DraftRoom) },
        )
    }
}
