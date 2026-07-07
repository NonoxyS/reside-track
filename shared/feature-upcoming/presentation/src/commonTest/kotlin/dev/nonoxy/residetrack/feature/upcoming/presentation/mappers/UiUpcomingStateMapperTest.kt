package dev.nonoxy.residetrack.feature.upcoming.presentation.mappers

import dev.nonoxy.residetrack.core.rooms.domain.upcoming.UpcomingBucket
import dev.nonoxy.residetrack.feature.upcoming.api.models.UpcomingItem
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiDaysBadge
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiOverdueUnit
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingBucket
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class UiUpcomingStateMapperTest {

    private val mapper: UiUpcomingStateMapper = UiUpcomingStateMapperImpl()

    @Test
    fun mapsDomainItemsToUiFields() {
        val ui = mapper.map(stateWith(daysLeft = -3))

        assertEquals(1, ui.items.size)
        val item = ui.items.first()
        assertEquals("14", item.roomNumber)
        assertEquals("2", item.floorNumber)
        assertEquals("305", item.streamNumber)

        assertEquals(UiUpcomingBucket.OVERDUE, item.bucket)
    }

    @Test
    fun formatsCheckOutDateAsDayMonthYearWithPadding() {
        val ui = mapper.map(stateWith(daysLeft = -3, checkOutDate = LocalDate(2026, 6, 1)))

        assertEquals("01.06.2026", ui.items.first().checkOutDate)
    }

    @Test
    fun buildsRemainingBadgeForFutureCheckout() {
        val badge = mapper.map(stateWith(daysLeft = 5)).items.first().daysBadge

        assertEquals(UiDaysBadge.Remaining(days = 5), badge)
    }

    @Test
    fun buildsOverdueBadgeInDaysBelowOneMonth() {
        val badge = mapper.map(stateWith(daysLeft = -3)).items.first().daysBadge

        assertEquals(UiDaysBadge.Overdue(amount = 3, unit = UiOverdueUnit.DAYS), badge)
    }

    @Test
    fun buildsOverdueBadgeInMonthsFromThirtyDays() {
        val badge = mapper.map(stateWith(daysLeft = -45)).items.first().daysBadge

        assertEquals(UiDaysBadge.Overdue(amount = 1, unit = UiOverdueUnit.MONTHS), badge)
    }

    @Test
    fun buildsOverdueBadgeInYearsFromThreeHundredSixtyFiveDays() {
        val badge = mapper.map(stateWith(daysLeft = -400)).items.first().daysBadge

        assertEquals(UiDaysBadge.Overdue(amount = 1, unit = UiOverdueUnit.YEARS), badge)
    }

    private fun stateWith(
        daysLeft: Int,
        checkOutDate: LocalDate = LocalDate(2026, 6, 1),
    ) = UpcomingStore.State(
        isLoading = false,
        isError = false,
        items = listOf(
            UpcomingItem(
                roomId = 7,
                floorNumber = 2,
                roomNumber = 14,
                studentId = 10,
                streamNumber = 305,
                checkOutDate = checkOutDate,
                daysLeft = daysLeft,
                bucket = UpcomingBucket.OVERDUE,
            ),
        ),
    )
}
