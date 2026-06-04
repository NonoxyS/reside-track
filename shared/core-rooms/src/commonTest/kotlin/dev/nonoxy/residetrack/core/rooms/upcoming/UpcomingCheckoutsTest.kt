package dev.nonoxy.residetrack.core.rooms.upcoming

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UpcomingCheckoutsTest {

    private val today = LocalDate(2026, 6, 4)

    @Test
    fun daysLeft_isSignedDifference() {
        assertEquals(-1, UpcomingCheckouts.daysLeft(LocalDate(2026, 6, 3), today))
        assertEquals(0, UpcomingCheckouts.daysLeft(today, today))
        assertEquals(7, UpcomingCheckouts.daysLeft(LocalDate(2026, 6, 11), today))
    }

    @Test
    fun isUpcoming_trueForOverdueThroughSeventhDay_falseBeyond() {
        assertTrue(UpcomingCheckouts.isUpcoming(LocalDate(2026, 6, 3), today)) // -1 overdue
        assertTrue(UpcomingCheckouts.isUpcoming(today, today)) // 0
        assertTrue(UpcomingCheckouts.isUpcoming(LocalDate(2026, 6, 11), today)) // 7
        assertFalse(UpcomingCheckouts.isUpcoming(LocalDate(2026, 6, 12), today)) // 8 out
    }

    @Test
    fun bucketOf_partitionsByUrgency() {
        assertEquals(UpcomingBucket.OVERDUE, UpcomingCheckouts.bucketOf(-1))
        assertEquals(UpcomingBucket.TODAY_TOMORROW, UpcomingCheckouts.bucketOf(0))
        assertEquals(UpcomingBucket.TODAY_TOMORROW, UpcomingCheckouts.bucketOf(1))
        assertEquals(UpcomingBucket.THIS_WEEK, UpcomingCheckouts.bucketOf(2))
        assertEquals(UpcomingBucket.THIS_WEEK, UpcomingCheckouts.bucketOf(7))
    }
}
