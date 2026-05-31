package dev.nonoxy.residetrack.feature.add_room.presentation.mappers

import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.residetrack.feature.add_room.presentation.models.UiAddRoomLabel

internal interface UiAddRoomLabelMapper {
    fun map(item: AddRoomStore.Label): UiAddRoomLabel
}

internal class UiAddRoomLabelMapperImpl : UiAddRoomLabelMapper {

    override fun map(item: AddRoomStore.Label): UiAddRoomLabel = when (item) {
        AddRoomStore.Label.CloseScreen -> UiAddRoomLabel.CloseScreen
        AddRoomStore.Label.NavigateToManageStudentsDraftRoom -> UiAddRoomLabel.NavigateToManageStudentsDraftRoom
        is AddRoomStore.Label.ShowSuccess -> UiAddRoomLabel.ShowSuccess(kind = item.kind)
        is AddRoomStore.Label.ShowError -> UiAddRoomLabel.ShowError(kind = item.kind)
    }
}
