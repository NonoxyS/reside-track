package dev.nonoxy.residetrack.feature.upcoming.presentation.mappers

import dev.nonoxy.residetrack.core.rooms.domain.upcoming.UpcomingBucket
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiDaysBadge
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiOverdueUnit
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingBucket
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingItem
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.char

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
                roomNumber = domain.roomNumber.toString(),
                floorNumber = domain.floorNumber.toString(),
                streamNumber = domain.streamNumber.toString(),
                checkOutDate = DISPLAY_DATE.format(domain.checkOutDate),
                daysBadge = daysBadge(domain.daysLeft),
                bucket = domain.bucket.toUi(),
            )
        }.toImmutableList(),
    )

    private fun daysBadge(daysLeft: Int): UiDaysBadge {
        if (daysLeft >= 0) return UiDaysBadge.Remaining(days = daysLeft)
        val overdue = -daysLeft
        return when {
            overdue >= DAYS_IN_YEAR -> UiDaysBadge.Overdue(overdue / DAYS_IN_YEAR, UiOverdueUnit.YEARS)
            overdue >= DAYS_IN_MONTH -> UiDaysBadge.Overdue(overdue / DAYS_IN_MONTH, UiOverdueUnit.MONTHS)
            else -> UiDaysBadge.Overdue(overdue, UiOverdueUnit.DAYS)
        }
    }

    private fun UpcomingBucket.toUi(): UiUpcomingBucket = when (this) {
        UpcomingBucket.OVERDUE -> UiUpcomingBucket.OVERDUE
        UpcomingBucket.TODAY_TOMORROW -> UiUpcomingBucket.TODAY_TOMORROW
        UpcomingBucket.THIS_WEEK -> UiUpcomingBucket.THIS_WEEK
    }

    private companion object {
        const val DAYS_IN_MONTH = 30
        const val DAYS_IN_YEAR = 365

        val DISPLAY_DATE = LocalDate.Format {
            dayOfMonth()
            char('.')
            monthNumber()
            char('.')
            year()
        }
    }
}
