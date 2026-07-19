package dev.nonoxy.residetrack.feature.upcoming.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import dev.nonoxy.residetrack.common.ui.theme.padding_size_4
import dev.nonoxy.residetrack.common.ui.theme.padding_size_8
import dev.nonoxy.residetrack.common.ui.theme.padding_size_20
import dev.nonoxy.residetrack.core.navigation.bottombar.LocalFloatingBottomBarInset
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingBucket
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingItem
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun UpcomingList(
    items: ImmutableList<UiUpcomingItem>,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.statusBarsPadding(),
        contentPadding = PaddingValues(
            start = padding_size_16,
            end = padding_size_16,
            top = padding_size_8,
            bottom = LocalFloatingBottomBarInset.current + padding_size_8,
        ),
        verticalArrangement = Arrangement.spacedBy(padding_size_8),
    ) {
        for (bucket in UiUpcomingBucket.entries) {
            val bucketItems = items.filter { it.bucket == bucket }
            if (bucketItems.isEmpty()) continue

            item(key = "header_${bucket.name}") {
                BucketHeader(bucket = bucket)
            }

            items(
                items = bucketItems,
                key = { item -> item.roomId },
            ) { item ->
                UpcomingListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    item = item,
                    onClick = onItemClick,
                )
            }
        }
    }
}

@Composable
private fun BucketHeader(bucket: UiUpcomingBucket) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = padding_size_20, bottom = padding_size_4),
        text = stringResource(bucket.title),
        style = ResideTrackTheme.typography.head5,
        color = ResideTrackTheme.colors.textCaption,
    )
}
