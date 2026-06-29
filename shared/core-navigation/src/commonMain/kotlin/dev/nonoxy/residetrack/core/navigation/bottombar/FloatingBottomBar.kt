package dev.nonoxy.residetrack.core.navigation.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_4
import dev.nonoxy.residetrack.common.ui.theme.padding_size_6
import dev.nonoxy.residetrack.common.ui.theme.padding_size_8
import dev.nonoxy.residetrack.common.ui.theme.padding_size_12
import dev.nonoxy.residetrack.common.ui.theme.size_64
import kotlinx.collections.immutable.ImmutableList

data class BottomBarItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0,
)

@Composable
fun FloatingBottomBar(
    items: ImmutableList<BottomBarItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(size_64),
        shape = ResideTrackTheme.shapes.cornerRadius40,
        color = ResideTrackTheme.colors.surface,
        shadowElevation = padding_size_8,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = padding_size_8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                BottomBarTab(
                    item = item,
                    selected = item.key == selectedKey,
                    onSelect = { onSelect(item.key) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun BottomBarTab(
    item: BottomBarItem,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) {
        ResideTrackTheme.colors.textAccent
    } else {
        ResideTrackTheme.colors.textCaption
    }
    Row(
        modifier = modifier
            .clip(ResideTrackTheme.shapes.cornerRadius40)
            .selectable(selected = selected, onClick = onSelect)
            .padding(horizontal = padding_size_12, vertical = padding_size_8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(padding_size_6, Alignment.CenterHorizontally),
    ) {
        Box {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = contentColor,
            )
            if (item.badgeCount > 0) {
                CountBadge(
                    count = item.badgeCount,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = padding_size_6, y = -padding_size_6),
                )
            }
        }
        Text(
            text = item.label,
            style = ResideTrackTheme.typography.head4,
            color = contentColor,
        )
    }
}

@Composable
private fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .clip(ResideTrackTheme.shapes.cornerRadius20)
            .background(ResideTrackTheme.colors.fillError)
            .padding(horizontal = padding_size_4),
        text = count.toString(),
        style = ResideTrackTheme.typography.caption,
        color = ResideTrackTheme.colors.white,
    )
}
