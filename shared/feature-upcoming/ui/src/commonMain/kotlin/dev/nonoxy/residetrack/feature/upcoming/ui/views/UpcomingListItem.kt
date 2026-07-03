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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_10
import dev.nonoxy.residetrack.common.ui.theme.padding_size_12
import dev.nonoxy.residetrack.common.ui.theme.padding_size_2
import dev.nonoxy.residetrack.common.ui.theme.padding_size_4
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
                text = stringResource(MR.strings.upcoming_room_label, item.roomNumber, item.floorNumber),
                style = ResideTrackTheme.typography.head3,
                color = ResideTrackTheme.colors.textBody,
            )
            Spacer(modifier = Modifier.height(padding_size_2))
            Text(
                text = "${stringResource(MR.strings.rooms_stream)} ${item.streamNumber}",
                style = ResideTrackTheme.typography.paragraph,
                color = ResideTrackTheme.colors.textCaption,
            )
            Text(
                text = item.checkOutDate,
                style = ResideTrackTheme.typography.caption,
                color = ResideTrackTheme.colors.textCaption,
            )
        }

        DaysLeftBadge(
            modifier = Modifier.padding(start = padding_size_10),
            label = item.daysLeftLabel,
            labelValue = item.daysLeftValue,
            bucket = item.bucket,
        )
    }
}

@Composable
private fun DaysLeftBadge(
    label: StringResource,
    labelValue: String,
    bucket: UiUpcomingBucket,
    modifier: Modifier = Modifier,
) {
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
        text = stringResource(label, labelValue),
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
                    checkOutDate = "2024-03-31",
                    daysLeft = -2,
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
                    checkOutDate = "2024-04-02",
                    daysLeft = 1,
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
                    checkOutDate = "2024-04-06",
                    daysLeft = 5,
                    bucket = UiUpcomingBucket.THIS_WEEK,
                ),
                onClick = {}
            )
        }
    }
}
