package dev.nonoxy.residetrack.core.notifications.domain

import dev.nonoxy.residetrack.core.database.entities.RoomEntity
import dev.nonoxy.residetrack.core.database.entities.StudentEntity
import dev.nonoxy.residetrack.core.database.relations.RoomWithStudents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant

class DigestBuilderTest {

    private val tz = TimeZone.UTC

    // Далёкое прошлое: ни один пуш не отсекается по fireAt (проверяем группировку/сортировку изолированно).
    private val distantPast = LocalDate(2026, 1, 1).atStartOfDayIn(tz)

    private fun epochOf(date: LocalDate): Long = date.atStartOfDayIn(tz).toEpochMilliseconds()

    private fun expectedFireAt(checkOutDate: LocalDate): Long {
        val fireDate = checkOutDate.minus(DatePeriod(days = DigestBuilder.LEAD_DAYS))
        return LocalDateTime(date = fireDate, time = DigestBuilder.fireTime).toInstant(tz).toEpochMilliseconds()
    }

    private fun room(roomNumber: Int, checkOutDates: List<LocalDate>) = RoomWithStudents(
        room = RoomEntity(floorNumber = roomNumber / 100, roomNumber = roomNumber, bedsCount = checkOutDates.size),
        students = checkOutDates.mapIndexed { index, date ->
            StudentEntity(
                roomId = roomNumber.toLong(),
                streamNumber = index,
                checkInDateEpochMillis = 0L,
                checkOutDateEpochMillis = epochOf(date),
            )
        },
    )

    @Test
    fun build_groupsByDate_distinctSortedRooms_placesCountsStudents() {
        val date = LocalDate(2026, 7, 10)
        val rooms = listOf(
            room(roomNumber = 401, checkOutDates = listOf(date, date)),
            room(roomNumber = 329, checkOutDates = listOf(date)),
        )

        val digests = DigestBuilder.build(rooms = rooms, now = distantPast, timeZone = tz)

        assertEquals(1, digests.size)
        val digest = digests.single()
        assertEquals(date, digest.date)
        assertEquals(listOf(329, 401), digest.roomNumbers)
        assertEquals(3, digest.placesCount)
        assertEquals(expectedFireAt(date), digest.fireAtEpochMillis)
    }

    @Test
    fun build_sameRoomMultipleStudentsSameDate_roomAppearsOnce_placesCountsAll() {
        val date = LocalDate(2026, 7, 10)
        val rooms = listOf(room(roomNumber = 329, checkOutDates = listOf(date, date, date)))

        val digest = DigestBuilder.build(rooms = rooms, now = distantPast, timeZone = tz).single()

        assertEquals(listOf(329), digest.roomNumbers)
        assertEquals(3, digest.placesCount)
    }

    @Test
    fun build_sortsDigestsByDateAscending() {
        val rooms = listOf(
            room(roomNumber = 101, checkOutDates = listOf(LocalDate(2026, 7, 15))),
            room(roomNumber = 102, checkOutDates = listOf(LocalDate(2026, 7, 5))),
            room(roomNumber = 103, checkOutDates = listOf(LocalDate(2026, 7, 10))),
        )

        val dates = DigestBuilder.build(rooms = rooms, now = distantPast, timeZone = tz).map { it.date }

        assertEquals(listOf(LocalDate(2026, 7, 5), LocalDate(2026, 7, 10), LocalDate(2026, 7, 15)), dates)
    }

    @Test
    fun build_dropsDigestWhoseFireAtIsInPast() {
        val now = LocalDate(2026, 7, 1).atStartOfDayIn(tz)
        val past = LocalDate(2026, 7, 2) // fireAt = 2026-06-29 09:00 -> до now
        val future = LocalDate(2026, 7, 10) // fireAt = 2026-07-07 09:00 -> после now
        val rooms = listOf(
            room(roomNumber = 201, checkOutDates = listOf(past)),
            room(roomNumber = 202, checkOutDates = listOf(future)),
        )

        val digests = DigestBuilder.build(rooms = rooms, now = now, timeZone = tz)

        assertEquals(listOf(future), digests.map { it.date })
    }

    @Test
    fun build_keepsDigestWhoseFireAtEqualsNow() {
        val date = LocalDate(2026, 7, 10)
        val fireAt = Instant.fromEpochMilliseconds(expectedFireAt(date))
        val rooms = listOf(room(roomNumber = 201, checkOutDates = listOf(date)))

        val digests = DigestBuilder.build(rooms = rooms, now = fireAt, timeZone = tz)

        assertEquals(1, digests.size)
    }

    @Test
    fun build_emptyInputReturnsEmpty() {
        assertTrue(DigestBuilder.build(rooms = emptyList(), now = distantPast, timeZone = tz).isEmpty())
    }

    @Test
    fun build_roomWithNoStudentsProducesNoDigest() {
        val rooms = listOf(room(roomNumber = 329, checkOutDates = emptyList()))

        assertTrue(DigestBuilder.build(rooms = rooms, now = distantPast, timeZone = tz).isEmpty())
    }
}
