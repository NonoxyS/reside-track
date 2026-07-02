package dev.nonoxy.residetrack.feature.upcoming.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_10
import dev.nonoxy.residetrack.common.ui.theme.padding_size_12
import dev.nonoxy.residetrack.common.ui.theme.padding_size_2
import dev.nonoxy.residetrack.common.ui.theme.padding_size_4
import dev.nonoxy.residetrack.common.ui.theme.padding_size_6
import dev.nonoxy.residetrack.common.ui.theme.size_16
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiDaysBadge
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiOverdueUnit
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingBucket
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingItem
import dev.nonoxy.residetrack.res.MR
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun UpcomingListItem(
    item: UiUpcomingItem,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ResideTrackTheme.colors.primary
) {
    Row(
        modifier = modifier
            .clip(ResideTrackTheme.shapes.cornerRadius16)
            .background(backgroundColor)
            .clickable(onClick = { onClick(item.roomId) })
            .padding(horizontal = padding_size_12, vertical = padding_size_10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(MR.strings.upcoming_room_number, item.roomNumber),
                style = ResideTrackTheme.typography.head3,
                color = ResideTrackTheme.colors.textBody,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(padding_size_2))
            Text(
                text = stringResource(MR.strings.upcoming_floor, item.floorNumber),
                style = ResideTrackTheme.typography.paragraph,
                color = ResideTrackTheme.colors.textBody,
            )
            Spacer(modifier = Modifier.height(padding_size_2))
            Text(
                text = "${stringResource(MR.strings.rooms_stream)} ${item.streamNumber}",
                style = ResideTrackTheme.typography.caption,
                color = ResideTrackTheme.colors.textCaption,
            )
        }

        Column(
            modifier = Modifier.padding(start = padding_size_10),
            horizontalAlignment = Alignment.End,
        ) {
            DaysLeftBadge(daysBadge = item.daysBadge, bucket = item.bucket)
            Spacer(modifier = Modifier.height(padding_size_6))
            CheckOutDate(date = item.checkOutDate)
        }
    }
}

@Composable
private fun CheckOutDate(
    date: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(size_16),
            imageVector = Icons.Filled.DateRange,
            contentDescription = null,
            tint = ResideTrackTheme.colors.textCaption,
        )
        Spacer(modifier = Modifier.size(padding_size_4))
        Text(
            text = date,
            style = ResideTrackTheme.typography.paragraph,
            color = ResideTrackTheme.colors.textBody,
        )
    }
}

@Composable
private fun DaysLeftBadge(
    daysBadge: UiDaysBadge,
    bucket: UiUpcomingBucket,
    modifier: Modifier = Modifier,
) {
    val text = when (daysBadge) {
        is UiDaysBadge.Remaining ->
            stringResource(MR.strings.upcoming_days_left, daysBadge.days.toString())

        is UiDaysBadge.Overdue -> {
            val resource = when (daysBadge.unit) {
                UiOverdueUnit.DAYS -> MR.strings.upcoming_days_overdue
                UiOverdueUnit.MONTHS -> MR.strings.upcoming_months_overdue
                UiOverdueUnit.YEARS -> MR.strings.upcoming_years_overdue
            }
            stringResource(resource, daysBadge.amount.toString())
        }
    }
    val backgroundColor = when (bucket) {
        UiUpcomingBucket.OVERDUE -> ResideTrackTheme.colors.fillErrorBGSecondary
        UiUpcomingBucket.TODAY_TOMORROW -> ResideTrackTheme.colors.fillWarningBGSecondary
        UiUpcomingBucket.THIS_WEEK -> ResideTrackTheme.colors.fillSuccessBGSecondary
    }
    val textColor = when (bucket) {
        UiUpcomingBucket.OVERDUE -> ResideTrackTheme.colors.textError
        UiUpcomingBucket.TODAY_TOMORROW -> ResideTrackTheme.colors.textWarning
        UiUpcomingBucket.THIS_WEEK -> ResideTrackTheme.colors.textSuccess
    }
    Text(
        modifier = modifier
            .clip(ResideTrackTheme.shapes.cornerRadius20)
            .background(backgroundColor)
            .padding(horizontal = padding_size_10, vertical = padding_size_4),
        text = text,
        style = ResideTrackTheme.typography.head5,
        color = textColor,
    )
}

@Preview
@Composable
private fun Preview() {
    ResideTrackTheme {
        Column(
            modifier = Modifier
                .background(ResideTrackTheme.colors.background)
                .padding(padding_size_12),
            verticalArrangement = Arrangement.spacedBy(padding_size_4),
        ) {
            UpcomingListItem(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                item = UiUpcomingItem(
                    roomId = 1,
                    roomNumber = "329",
                    floorNumber = "3",
                    streamNumber = "1234",
                    checkOutDate = "31.03.2024",
                    daysBadge = UiDaysBadge.Overdue(amount = 2, unit = UiOverdueUnit.DAYS),
                    bucket = UiUpcomingBucket.OVERDUE,
                ),
                onClick = {}
            )
            UpcomingListItem(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                item = UiUpcomingItem(
                    roomId = 2,
                    roomNumber = "401",
                    floorNumber = "4",
                    streamNumber = "5646",
                    checkOutDate = "02.04.2024",
                    daysBadge = UiDaysBadge.Remaining(days = 1),
                    bucket = UiUpcomingBucket.TODAY_TOMORROW,
                ),
                onClick = {}
            )
            UpcomingListItem(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                item = UiUpcomingItem(
                    roomId = 3,
                    roomNumber = "215",
                    floorNumber = "2",
                    streamNumber = "7777",
                    checkOutDate = "06.04.2023",
                    daysBadge = UiDaysBadge.Overdue(amount = 1, unit = UiOverdueUnit.YEARS),
                    bucket = UiUpcomingBucket.OVERDUE,
                ),
                onClick = {}
            )
        }
    }
}
