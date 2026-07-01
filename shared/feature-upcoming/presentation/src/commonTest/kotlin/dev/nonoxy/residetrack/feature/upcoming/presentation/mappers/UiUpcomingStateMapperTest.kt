package dev.nonoxy.residetrack.feature.upcoming.presentation.mappers

import dev.nonoxy.residetrack.core.rooms.domain.upcoming.UpcomingBucket
import dev.nonoxy.residetrack.feature.upcoming.api.models.UpcomingItem
import dev.nonoxy.residetrack.feature.upcoming.api.store.UpcomingStore
import dev.nonoxy.residetrack.feature.upcoming.presentation.models.UiUpcomingBucket
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class UiUpcomingStateMapperTest {

    private val mapper: UiUpcomingStateMapper = UiUpcomingStateMapperImpl()

    @Test
    fun mapsDomainItemsToUiWithRoomLabel() {
        val state = UpcomingStore.State(
            isLoading = false,
            isError = false,
            items = listOf(
                UpcomingItem(
                    roomId = 7,
                    floorNumber = 2,
                    roomNumber = 14,
                    studentId = 10,
                    streamNumber = 305,
                    checkOutDate = LocalDate(2026, 6, 1),
                    daysLeft = -3,
                    bucket = UpcomingBucket.OVERDUE,
                )
            ),
        )

        val ui = mapper.map(state)

        assertEquals(1, ui.items.size)
        val item = ui.items.first()
        assertEquals("14", item.roomNumber)
        assertEquals("2", item.floorNumber)
        assertEquals("305", item.streamNumber)
        assertEquals("2026-06-01", item.checkOutDate)
        assertEquals(-3, item.daysLeft)
        assertEquals(UiUpcomingBucket.OVERDUE, item.bucket)
    }
}
