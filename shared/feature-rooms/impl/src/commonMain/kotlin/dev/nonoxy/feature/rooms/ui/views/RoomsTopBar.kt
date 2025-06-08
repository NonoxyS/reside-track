package dev.nonoxy.feature.rooms.ui.views

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
import dev.nonoxy.core.design.theme.ResideTrackTheme
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import residetrack.shared.feature_rooms.impl.generated.resources.Res
import residetrack.shared.feature_rooms.impl.generated.resources.ic_add
import residetrack.shared.feature_rooms.impl.generated.resources.rooms_available_places
import residetrack.shared.feature_rooms.impl.generated.resources.rooms_total_places

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
                    imageVector = vectorResource(resource = Res.drawable.ic_add),
                    contentDescription = null
                )
            }
        },
        navigationIcon = {
            Column {
                Text(
                    text = pluralStringResource(
                        resource = Res.plurals.rooms_total_places,
                        quantity = totalPlaces,
                        totalPlaces
                    ),
                    style = ResideTrackTheme.typography.lead
                )

                Text(
                    text = pluralStringResource(
                        resource = Res.plurals.rooms_available_places,
                        quantity = availablePlaces,
                        availablePlaces
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
