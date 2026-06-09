package dev.nonoxy.residetrack.feature.upcoming.presentation.mappers

import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingLabel

internal interface UiUpcomingLabelMapper {
    fun map(item: UpcomingStore.Label): UiUpcomingLabel
}

internal class UiUpcomingLabelMapperImpl : UiUpcomingLabelMapper {

    override fun map(item: UpcomingStore.Label): UiUpcomingLabel = when (item) {
        is UpcomingStore.Label.NavigateToManageStudentsExistingRoom ->
            UiUpcomingLabel.NavigateToManageStudentsExistingRoom(roomId = item.roomId)
    }
}
