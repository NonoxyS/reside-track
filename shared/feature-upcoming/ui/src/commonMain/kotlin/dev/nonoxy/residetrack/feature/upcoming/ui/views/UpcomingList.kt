package dev.nonoxy.residetrack.feature.upcoming.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import dev.nonoxy.residetrack.common.ui.theme.padding_size_85
import dev.nonoxy.residetrack.common.ui.theme.padding_size_20
import dev.nonoxy.residetrack.core.rooms.upcoming.UpcomingBucket
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingItem
import dev.nonoxy.residetrack.res.MR
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun UpcomingList(
    items: ImmutableList<UiUpcomingItem>,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = padding_size_16,
            end = padding_size_16,
            top = padding_size_8,
            bottom = padding_size_85,
        ),
        verticalArrangement = Arrangement.spacedBy(padding_size_8),
    ) {
        for (bucket in UpcomingBucket.entries) {
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
private fun BucketHeader(bucket: UpcomingBucket) {
    val title = when (bucket) {
        UpcomingBucket.OVERDUE -> MR.strings.upcoming_bucket_overdue
        UpcomingBucket.TODAY_TOMORROW -> MR.strings.upcoming_bucket_today_tomorrow
        UpcomingBucket.THIS_WEEK -> MR.strings.upcoming_bucket_this_week
    }
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = padding_size_20, bottom = padding_size_4),
        text = stringResource(title),
        style = ResideTrackTheme.typography.head5,
        color = ResideTrackTheme.colors.textCaption,
    )
}
