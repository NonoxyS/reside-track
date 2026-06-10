package dev.nonoxy.residetrack.ui.tabcontainer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.core.navigation.bottombar.BottomBarItem
import dev.nonoxy.residetrack.core.navigation.bottombar.FloatingBottomBar
import dev.nonoxy.residetrack.core.navigation.bottombar.LocalFloatingBottomBarInset
import dev.nonoxy.residetrack.feature.rooms.ui.api.RoomsRoute
import dev.nonoxy.residetrack.feature.upcoming.ui.api.UpcomingRoute
import dev.nonoxy.residetrack.navigation.TabContainerNavHost
import dev.nonoxy.residetrack.res.MR
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel

private const val ROOMS_KEY = "rooms"
private const val UPCOMING_KEY = "upcoming"

@Composable
internal fun TabContainerScreen(
    onNavigateToAddRoomScreen: () -> Unit,
    onNavigateToRoomEditorExistingRoom: (String) -> Unit,
    viewModel: TabContainerViewModel = koinViewModel(),
) {
    val innerNavController = rememberNavController()
    val backStackEntry by innerNavController.currentBackStackEntryAsState()
    val badgeCount by viewModel.upcomingCount.collectAsStateWithLifecycle()

    val selectedKey = if (backStackEntry?.destination?.hasRoute(UpcomingRoute::class) == true) {
        UPCOMING_KEY
    } else {
        ROOMS_KEY
    }

    val items = persistentListOf(
        BottomBarItem(
            key = ROOMS_KEY,
            label = stringResource(MR.strings.rooms_tab),
            icon = Icons.Filled.Home,
        ),
        BottomBarItem(
            key = UPCOMING_KEY,
            label = stringResource(MR.strings.upcoming_tab),
            icon = Icons.Filled.DateRange,
            badgeCount = badgeCount,
        ),
    )

    var barHeightPx by remember { mutableIntStateOf(0) }
    val barInset = with(LocalDensity.current) { barHeightPx.toDp() }

    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalFloatingBottomBarInset provides barInset) {
            TabContainerNavHost(
                navController = innerNavController,
                onNavigateToAddRoomScreen = onNavigateToAddRoomScreen,
                onNavigateToRoomEditorExistingRoom = onNavigateToRoomEditorExistingRoom,
                modifier = Modifier.fillMaxSize(),
            )
        }
        FloatingBottomBar(
            items = items,
            selectedKey = selectedKey,
            onSelect = { key ->
                val route = if (key == UPCOMING_KEY) UpcomingRoute else RoomsRoute
                innerNavController.navigate(route) {
                    popUpTo(innerNavController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { barHeightPx = it.height }
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}
