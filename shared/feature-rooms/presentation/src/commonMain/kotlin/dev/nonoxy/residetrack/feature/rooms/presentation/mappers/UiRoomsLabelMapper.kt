package dev.nonoxy.residetrack.feature.rooms.presentation.mappers

import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore
import dev.nonoxy.residetrack.feature.rooms.presentation.models.UiRoomsLabel

internal interface UiRoomsLabelMapper {
    fun map(item: RoomsStore.Label): UiRoomsLabel
}

internal class UiRoomsLabelMapperImpl : UiRoomsLabelMapper {

    override fun map(item: RoomsStore.Label): UiRoomsLabel = when (item) {
        RoomsStore.Label.NavigateToAddRoomScreen ->
            UiRoomsLabel.NavigateToAddRoomScreen

        is RoomsStore.Label.NavigateToManageStudentsExistingRoom ->
            UiRoomsLabel.NavigateToManageStudentsExistingRoom(roomId = item.roomId)
    }
}
