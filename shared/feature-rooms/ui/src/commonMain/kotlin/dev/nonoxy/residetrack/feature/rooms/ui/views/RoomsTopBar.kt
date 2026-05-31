package dev.nonoxy.residetrack.feature.rooms.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.res.MR
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomsTopBar(
    totalPlaces: Int,
    availablePlaces: Int,
    onAddRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = ResideTrackTheme.colors.background,
        navigationIconContentColor = ResideTrackTheme.colors.textCaption,
        actionIconContentColor = ResideTrackTheme.colors.textCaption
    ),
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {},
        colors = colors,
        actions = {
            IconButton(onClick = onAddRoomClick) {
                Icon(
                    painter = painterResource(MR.images.ic_add),
                    contentDescription = null
                )
            }
        },
        navigationIcon = {
            Column {
                Text(
                    text = stringResource(
                        resource = MR.plurals.rooms_total_places,
                        quantity = totalPlaces,
                        totalPlaces,
                    ),
                    style = ResideTrackTheme.typography.lead
                )

                Text(
                    text = stringResource(
                        resource = MR.plurals.rooms_available_places,
                        quantity = availablePlaces,
                        availablePlaces,
                    ),
                    style = ResideTrackTheme.typography.lead
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun Preview() {
    ResideTrackTheme {
        RoomsTopBar(
            totalPlaces = 617,
            availablePlaces = 613,
            onAddRoomClick = {}
        )
    }
}
