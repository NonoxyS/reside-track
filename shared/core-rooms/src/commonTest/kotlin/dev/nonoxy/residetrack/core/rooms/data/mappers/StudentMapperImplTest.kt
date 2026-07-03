package dev.nonoxy.residetrack.core.rooms.data.mappers

import dev.nonoxy.residetrack.core.database.entities.StudentEntity
import dev.nonoxy.residetrack.core.rooms.domain.model.Student
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class StudentMapperImplTest {

    private val mapper = StudentMapperImpl()

    @Test
    fun `entity millis are read as their UTC calendar day and not the device-local one`() {
        // 1970-01-01T23:00Z. Read in UTC it stays 1970-01-01; any positive-offset local
        // timezone (e.g. the CI runner) would roll it into 1970-01-02, pinning read to UTC.
        val lateInUtcDay = 23L * 60 * 60 * 1000
        val entity = studentEntity(checkInMillis = lateInUtcDay, checkOutMillis = lateInUtcDay)

        val student = mapper.map(entity)

        assertEquals(LocalDate(1970, 1, 1), student.checkInDate)
        assertEquals(LocalDate(1970, 1, 1), student.checkOutDate)
    }

    @Test
    fun `domain dates are written as UTC-midnight millis and not the device-local ones`() {
        val student = student(checkIn = LocalDate(1970, 1, 1), checkOut = LocalDate(1970, 1, 1))

        val entity = mapper.map(student, roomId = 1)

        assertEquals(0L, entity.checkInDateEpochMillis)
        assertEquals(0L, entity.checkOutDateEpochMillis)
    }

    @Test
    fun `dates survive an entity to domain to entity round trip in any timezone`() {
        val student = student(checkIn = LocalDate(2026, 7, 1), checkOut = LocalDate(2026, 7, 10))

        val roundTripped = mapper.map(mapper.map(student, roomId = 1))

        assertEquals(LocalDate(2026, 7, 1), roundTripped.checkInDate)
        assertEquals(LocalDate(2026, 7, 10), roundTripped.checkOutDate)
    }

    private fun studentEntity(checkInMillis: Long, checkOutMillis: Long) = StudentEntity(
        id = 1,
        roomId = 1,
        streamNumber = 305,
        checkInDateEpochMillis = checkInMillis,
        checkOutDateEpochMillis = checkOutMillis,
    )

    private fun student(checkIn: LocalDate, checkOut: LocalDate) = Student(
        id = 1,
        streamNumber = 305,
        checkInDate = checkIn,
        checkOutDate = checkOut,
        isCheckOutDateNearOrExpired = false,
    )
}
