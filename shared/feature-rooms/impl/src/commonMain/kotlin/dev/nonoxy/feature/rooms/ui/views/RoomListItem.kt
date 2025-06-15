package dev.nonoxy.feature.rooms.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastForEach
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.core.design.theme.padding_size_12
import dev.nonoxy.core.design.theme.padding_size_4
import dev.nonoxy.core.design.theme.padding_size_8
import dev.nonoxy.feature.rooms.ui.models.UiRoom
import dev.nonoxy.feature.rooms.ui.models.UiStudent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import residetrack.shared.feature_rooms.impl.generated.resources.Res
import residetrack.shared.feature_rooms.impl.generated.resources.rooms_check_in
import residetrack.shared.feature_rooms.impl.generated.resources.rooms_check_out
import residetrack.shared.feature_rooms.impl.generated.resources.rooms_floor
import residetrack.shared.feature_rooms.impl.generated.resources.rooms_room
import residetrack.shared.feature_rooms.impl.generated.resources.rooms_stream

@Composable
internal fun RoomListItem(
    uiRoom: UiRoom,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ResideTrackTheme.colors.primary
) {
    Row(
        modifier = modifier
            .clip(ResideTrackTheme.shapes.cornerRadius16)
            .background(backgroundColor)
            .clickable(onClick = { onClick(uiRoom.id) })
            .padding(padding_size_8),
    ) {
        RoomInfo(
            modifier = Modifier.padding(end = padding_size_8),
            floor = uiRoom.floorNumber,
            room = uiRoom.roomNumber,
            totalBeds = uiRoom.bedsCount,
            occupiedBeds = uiRoom.students.size.toString(),
            colorForSizeHolderText = backgroundColor
        )

        VerticalDivider(color = ResideTrackTheme.colors.textCaption)

        StudentsStreamInfo(
            modifier = Modifier.padding(horizontal = padding_size_8),
            students = uiRoom.students
        )

        VerticalDivider(color = ResideTrackTheme.colors.textCaption)

        StudentsCheckInOutInfo(
            modifier = Modifier
                .padding(start = padding_size_12)
                .fillMaxWidth(),
            students = uiRoom.students
        )
    }
}

@Composable
private fun RoomInfo(
    floor: String,
    room: String,
    totalBeds: String,
    occupiedBeds: String,
    colorForSizeHolderText: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = buildString {
                append(stringResource(resource = Res.string.rooms_floor))
                append(": $floor")
            },
            style = ResideTrackTheme.typography.head4,
            color = ResideTrackTheme.colors.textCaption,
        )

        Box {
            Text(
                text = buildString {
                    append(stringResource(resource = Res.string.rooms_room))
                    append(": 12345") // Hold exactly for 5-symbol room number
                },
                style = ResideTrackTheme.typography.head4,
                color = colorForSizeHolderText
            )

            Text(
                text = buildString {
                    append(stringResource(resource = Res.string.rooms_room))
                    append(": $room")
                },
                style = ResideTrackTheme.typography.head4,
                color = ResideTrackTheme.colors.textCaption,
            )
        }

        Text(
            text = "$occupiedBeds / $totalBeds",
            style = ResideTrackTheme.typography.head4,
            color = ResideTrackTheme.colors.textCaption,
        )
    }
}

@Composable
private fun StudentsStreamInfo(
    modifier: Modifier = Modifier,
    students: ImmutableList<UiStudent>
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(resource = Res.string.rooms_stream),
            style = ResideTrackTheme.typography.head4,
            color = ResideTrackTheme.colors.textCaption,
        )
        Spacer(modifier = Modifier.height(padding_size_4))

        students.fastForEach { student ->
            Text(
                text = student.streamNumber,
                style = ResideTrackTheme.typography.head4,
                color = ResideTrackTheme.colors.textCaption,
            )
        }
    }
}

@Composable
private fun StudentsCheckInOutInfo(
    modifier: Modifier = Modifier,
    students: ImmutableList<UiStudent>
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(resource = Res.string.rooms_check_in),
                style = ResideTrackTheme.typography.head4,
                color = ResideTrackTheme.colors.textCaption,
            )
            Spacer(modifier = Modifier.height(padding_size_4))

            students.fastForEach { student ->
                Text(
                    text = student.checkInDate,
                    style = ResideTrackTheme.typography.head4,
                    color = ResideTrackTheme.colors.textCaption,
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(resource = Res.string.rooms_check_out),
                style = ResideTrackTheme.typography.head4,
                color = ResideTrackTheme.colors.textCaption,
            )
            Spacer(modifier = Modifier.height(padding_size_4))

            students.fastForEach { student ->
                Text(
                    text = student.checkOutDate,
                    style = ResideTrackTheme.typography.head4,
                    color = if (student.isCheckOutDateNearOrExpired) {
                        ResideTrackTheme.colors.textError
                    } else {
                        ResideTrackTheme.colors.textCaption
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ResideTrackTheme {
        RoomListItem(
            modifier = Modifier.height(IntrinsicSize.Min).fillMaxWidth()
                .background(ResideTrackTheme.colors.background),
            uiRoom = UiRoom(
                id = 1,
                floorNumber = "3",
                roomNumber = "329",
                bedsCount = "5",
                students = persistentListOf(
                    UiStudent(
                        streamNumber = "1234",
                        checkInDate = "12-03-2024",
                        checkOutDate = "31-03-2024",
                        isCheckOutDateNearOrExpired = false
                    ),
                    UiStudent(
                        streamNumber = "5646",
                        checkInDate = "12-03-2024",
                        checkOutDate = "15-03-2024",
                        isCheckOutDateNearOrExpired = true
                    ),
                    UiStudent(
                        streamNumber = "1234",
                        checkInDate = "12-03-2024",
                        checkOutDate = "31-03-2024",
                        isCheckOutDateNearOrExpired = false
                    ),
                    UiStudent(
                        streamNumber = "5646",
                        checkInDate = "12-03-2024",
                        checkOutDate = "15-03-2024",
                        isCheckOutDateNearOrExpired = true
                    ),
                    UiStudent(
                        streamNumber = "1234",
                        checkInDate = "12-03-2024",
                        checkOutDate = "31-03-2024",
                        isCheckOutDateNearOrExpired = false
                    ),
                    UiStudent(
                        streamNumber = "5646",
                        checkInDate = "12-03-2024",
                        checkOutDate = "15-03-2024",
                        isCheckOutDateNearOrExpired = true
                    ),
                )
            ),
            onClick = {}
        )
    }
}
