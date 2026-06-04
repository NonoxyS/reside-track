package dev.nonoxy.residetrack.feature.upcoming.presentation.mappers

import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingItem
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingState
import kotlinx.collections.immutable.toImmutableList

internal interface UiUpcomingStateMapper {
    fun map(item: UpcomingStore.State): UiUpcomingState
}

internal class UiUpcomingStateMapperImpl : UiUpcomingStateMapper {

    override fun map(item: UpcomingStore.State): UiUpcomingState = UiUpcomingState(
        isLoading = item.isLoading,
        isError = item.isError,
        items = item.items.map { domain ->
            UiUpcomingItem(
                roomId = domain.roomId,
                roomLabel = "${domain.roomNumber} · эт. ${domain.floorNumber}",
                streamNumber = domain.streamNumber.toString(),
                checkOutDate = domain.checkOutDate.toString(),
                daysLeft = domain.daysLeft,
                bucket = domain.bucket,
            )
        }.toImmutableList(),
    )
}
