package dev.nonoxy.residetrack.feature.upcoming.ui.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nonoxy.residetrack.common.ui.common.state.ShowStateData
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.core.rooms.upcoming.UpcomingBucket
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingItem
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingState
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun UpcomingScreenDetails(
    state: UiUpcomingState,
    onItemClick: (Long) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ShowStateData(
        modifier = modifier,
        state = state,
        isLoading = state.isLoading,
        isError = state.isError,
        onRetryClick = onRetryClick,
    ) { currentState ->
        if (currentState.items.isEmpty()) {
            UpcomingEmptyState(modifier = Modifier.fillMaxSize())
            return@ShowStateData
        }

        UpcomingList(
            modifier = Modifier.fillMaxSize(),
            items = currentState.items,
            onItemClick = onItemClick,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    ResideTrackTheme {
        UpcomingScreenDetails(
            state = UiUpcomingState(
                items = persistentListOf(
                    UiUpcomingItem(
                        roomId = 1,
                        roomNumber = "329",
                        floorNumber = "3",
                        streamNumber = "1234",
                        checkOutDate = "2024-03-31",
                        daysLeft = -2,
                        bucket = UpcomingBucket.OVERDUE,
                    ),
                    UiUpcomingItem(
                        roomId = 2,
                        roomNumber = "401",
                        floorNumber = "4",
                        streamNumber = "5646",
                        checkOutDate = "2024-04-02",
                        daysLeft = 1,
                        bucket = UpcomingBucket.TODAY_TOMORROW,
                    ),
                )
            ),
            onItemClick = {},
            onRetryClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
