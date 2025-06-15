package dev.nonoxy.feature.rooms.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nonoxy.core.design.theme.padding_size_16
import dev.nonoxy.core.design.theme.padding_size_20
import dev.nonoxy.core.design.theme.padding_size_8
import dev.nonoxy.feature.rooms.ui.models.UiRoom
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun RoomList(
    rooms: ImmutableList<UiRoom>,
    onRoomClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = padding_size_16,
            vertical = padding_size_20
        ),
        verticalArrangement = Arrangement.spacedBy(padding_size_8)
    ) {
        items(
            items = rooms,
            key = { item -> item.id }
        ) { item ->
            RoomListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                uiRoom = item,
                onClick = onRoomClick
            )
        }
    }
}
