package dev.nonoxy.residetrack.feature.upcoming.impl.domain

import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import dev.nonoxy.residetrack.core.rooms.domain.model.Student
import dev.nonoxy.residetrack.core.rooms.domain.upcoming.UpcomingBucket
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class UpcomingMapperTest {

    private val today = LocalDate(2026, 6, 4)

    private fun student(id: Long, stream: Int, checkOut: LocalDate) = Student(
        id = id,
        streamNumber = stream,
        checkInDate = LocalDate(2026, 1, 1),
        checkOutDate = checkOut,
        isCheckOutDateNearOrExpired = false,
    )

    private fun room(id: Long, floor: Int, number: Int, students: List<Student>) =
        Room(id = id, floorNumber = floor, roomNumber = number, bedsCount = 4, students = students)

    @Test
    fun keepsOnlyStudentsWithinSevenDaysOrOverdue_sortedByDaysLeft() {
        val rooms = listOf(
            room(
                1,
                2,
                14,
                listOf(
                    student(10, 305, LocalDate(2026, 6, 1)), // -3 overdue, keep
                    student(11, 309, LocalDate(2026, 6, 30)), // +26, drop
                )
            ),
            room(
                2,
                3,
                21,
                listOf(
                    student(20, 118, LocalDate(2026, 6, 11)), // +7, keep
                    student(21, 402, LocalDate(2026, 6, 5)), // +1, keep
                )
            ),
        )

        val items = UpcomingMapper.map(rooms = rooms, today = today)

        assertEquals(listOf(305, 402, 118), items.map { it.streamNumber }) // sorted asc daysLeft
        assertEquals(3, items.size)
    }

    @Test
    fun assignsBucketsAndCarriesRoomCoordinates() {
        val rooms = listOf(
            room(7, 2, 14, listOf(student(10, 305, LocalDate(2026, 6, 1)))),
        )

        val item = UpcomingMapper.map(rooms = rooms, today = today).single()

        assertEquals(UpcomingBucket.OVERDUE, item.bucket)
        assertEquals(-3, item.daysLeft)
        assertEquals(7L, item.roomId)
        assertEquals(2, item.floorNumber)
        assertEquals(14, item.roomNumber)
        assertEquals(10L, item.studentId)
    }
}
