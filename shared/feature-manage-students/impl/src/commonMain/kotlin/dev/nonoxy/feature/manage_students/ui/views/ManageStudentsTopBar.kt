package dev.nonoxy.feature.manage_students.ui.views

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import dev.nonoxy.core.design.theme.ResideTrackTheme
import dev.nonoxy.feature.manage_students.ui.models.UiRoom
import org.jetbrains.compose.resources.stringResource
import residetrack.shared.feature_manage_students.impl.generated.resources.Res
import residetrack.shared.feature_manage_students.impl.generated.resources.manage_students_room_title
import residetrack.shared.feature_manage_students.impl.generated.resources.manage_students_title

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
                        Res.string.manage_students_room_title, 
                        it.floorNumber.toIntOrNull() ?: 0, 
                        it.roomNumber.toIntOrNull() ?: 0
                    ) 
                } ?: stringResource(Res.string.manage_students_title),
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