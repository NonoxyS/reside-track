package dev.nonoxy.residetrack.feature.upcoming.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.core.rooms.upcoming.UpcomingBucket
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingItem
import dev.nonoxy.residetrack.res.MR

@Composable
internal fun UpcomingListItem(
    item: UiUpcomingItem,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick(item.roomId) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(MR.strings.upcoming_room_label, item.roomNumber, item.floorNumber),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${stringResource(MR.strings.rooms_stream)} ${item.streamNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = item.checkOutDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val badgeText = if (item.daysLeft < 0) {
                stringResource(MR.strings.upcoming_days_overdue, (-item.daysLeft).toString())
            } else {
                stringResource(MR.strings.upcoming_days_left, item.daysLeft.toString())
            }
            val badgeColor = when (item.bucket) {
                UpcomingBucket.OVERDUE -> MaterialTheme.colorScheme.errorContainer
                UpcomingBucket.TODAY_TOMORROW -> MaterialTheme.colorScheme.tertiaryContainer
                UpcomingBucket.THIS_WEEK -> MaterialTheme.colorScheme.secondaryContainer
            }
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(badgeColor)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}
