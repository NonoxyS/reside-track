package dev.nonoxy.feature.rooms.ui.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import kotlinx.collections.immutable.ImmutableSet
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.res.MR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomsTabRow(
    floors: ImmutableSet<Int>,
    selectedTabIndex: Int,
    onTabClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (floors.size <= 3) {
        TabRow(
            modifier = modifier.fillMaxWidth(),
            selectedTabIndex = selectedTabIndex,
            containerColor = ResideTrackTheme.colors.background,
            contentColor = ResideTrackTheme.colors.textAccent,
            divider = {},
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(it[selectedTabIndex]),
                    width = Dp.Unspecified,
                    color = ResideTrackTheme.colors.textAccent
                )
            },
        ) {
            floors.forEachIndexed { index, floor ->
                Tab(
                    modifier = Modifier.fillMaxWidth(),
                    text = {
                        Text(
                            text = buildString {
                                append(stringResource(MR.strings.rooms_floor))
                                append(" $floor")
                            },
                            style = ResideTrackTheme.typography.lead
                        )
                    },
                    unselectedContentColor = ResideTrackTheme.colors.textCaption,
                    selected = selectedTabIndex == index,
                    onClick = { onTabClick(index) }
                )
            }
        }
    } else {
        ScrollableTabRowImpl(
            modifier = modifier.fillMaxWidth(),
            selectedTabIndex = selectedTabIndex,
            containerColor = ResideTrackTheme.colors.background,
            contentColor = ResideTrackTheme.colors.textAccent,
            edgePadding = padding_size_16,
            divider = {},
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = true),
                    width = Dp.Unspecified,
                    color = ResideTrackTheme.colors.textAccent
                )
            },
        ) {
            floors.forEachIndexed { index, floor ->
                Tab(
                    modifier = Modifier.fillMaxWidth(),
                    text = {
                        Text(
                            text = buildString {
                                append(stringResource(MR.strings.rooms_floor))
                                append(" $floor")
                            },
                            style = ResideTrackTheme.typography.lead
                        )
                    },
                    unselectedContentColor = ResideTrackTheme.colors.textCaption,
                    selected = selectedTabIndex == index,
                    onClick = { onTabClick(index) }
                )
            }
        }
    }
}
