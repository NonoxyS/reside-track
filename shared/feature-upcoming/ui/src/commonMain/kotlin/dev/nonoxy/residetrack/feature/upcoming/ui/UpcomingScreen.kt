package dev.nonoxy.residetrack.feature.upcoming.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.common.ui.common.utils.CollectFlow
import dev.nonoxy.residetrack.core.rooms.upcoming.UpcomingBucket
import dev.nonoxy.residetrack.feature.upcoming.presentation.UpcomingViewModel
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingItem
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingLabel
import dev.nonoxy.residetrack.feature.upcoming.ui.views.UpcomingListItem
import dev.nonoxy.residetrack.res.MR
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun UpcomingScreen(
    onNavigateToManageStudentsExistingRoom: (String) -> Unit,
    viewModel: UpcomingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            is UiUpcomingLabel.NavigateToManageStudentsExistingRoom ->
                onNavigateToManageStudentsExistingRoom(label.roomId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            state.isError -> {
                ErrorContent(
                    onRetry = viewModel::onRetryClick,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            state.items.isEmpty() -> {
                EmptyContent(modifier = Modifier.align(Alignment.Center))
            }
            else -> {
                UpcomingList(
                    items = state.items,
                    onItemClick = viewModel::onStudentClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun UpcomingList(
    items: List<UiUpcomingItem>,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (bucket in UpcomingBucket.entries) {
            val bucketItems = items.filter { it.bucket == bucket }
            if (bucketItems.isEmpty()) continue

            val headerText = when (bucket) {
                UpcomingBucket.OVERDUE -> MR.strings.upcoming_bucket_overdue
                UpcomingBucket.TODAY_TOMORROW -> MR.strings.upcoming_bucket_today_tomorrow
                UpcomingBucket.THIS_WEEK -> MR.strings.upcoming_bucket_this_week
            }

            item(key = "header_${bucket.name}") {
                Text(
                    text = stringResource(headerText),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                )
            }

            items(
                items = bucketItems,
                key = { item -> item.roomId },
            ) { item ->
                UpcomingListItem(
                    item = item,
                    onClick = onItemClick,
                )
            }
        }
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(MR.strings.upcoming_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(MR.strings.upcoming_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(MR.strings.error_something_went_wrong),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onRetry) {
            Text(text = stringResource(MR.strings.action_retry))
        }
    }
}
