package dev.nonoxy.feature.manage_students.ui.views

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.feature.manage_students.presentation.models.UiRoom
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManageStudentsTopBar(
    room: UiRoom?,
    onCloseClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = room?.let {
                    stringResource(
                        MR.strings.manage_students_room_title,
                        it.floorNumber.toIntOrNull() ?: 0,
                        it.roomNumber.toIntOrNull() ?: 0
                    )
                } ?: stringResource(MR.strings.manage_students_title),
                style = ResideTrackTheme.typography.head3,
                color = ResideTrackTheme.colors.textPrimary
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ResideTrackTheme.colors.background,
            titleContentColor = ResideTrackTheme.colors.textPrimary
        )
    )
}
