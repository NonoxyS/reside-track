package dev.nonoxy.residetrack.feature.rooms.presentation.mappers

import dev.nonoxy.residetrack.core.backup.domain.model.Backup
import dev.nonoxy.residetrack.core.backup.domain.model.BackupRoom
import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import dev.nonoxy.residetrack.core.rooms.domain.model.Student
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UiRoomsStateMapperTest {

    private val mapper: UiRoomsStateMapper =
        UiRoomsStateMapperImpl(roomMapper = UiRoomMapperImpl(studentMapper = UiStudentMapperImpl()))

    private fun student(stream: Int) = Student(
        id = stream.toLong(),
        streamNumber = stream,
        checkInDate = LocalDate(2026, 1, 1),
        checkOutDate = LocalDate(2026, 6, 1),
        isCheckOutDateNearOrExpired = false,
    )

    private fun room(id: Long, floor: Int, number: Int, beds: Int, students: List<Student>) =
        Room(id = id, floorNumber = floor, roomNumber = number, bedsCount = beds, students = students)

    @Test
    fun `sums total and available places across all floors`() {
        val state = RoomsStore.State(
            roomsOnFloor = mapOf(
                2 to listOf(
                    room(id = 1, floor = 2, number = 21, beds = 4, students = listOf(student(305), student(306))),
                ),
                3 to listOf(
                    room(id = 2, floor = 3, number = 31, beds = 3, students = listOf(student(118))),
                ),
            ),
        )

        val ui = mapper.map(state)

        assertEquals(7, ui.totalPlaces) // 4 + 3
        assertEquals(4, ui.availablePlaces) // 7 - 3 occupied
        assertEquals(setOf(2, 3), ui.roomsOnFloor.keys)
        assertEquals("21", ui.roomsOnFloor.getValue(2).first().roomNumber)
        assertEquals(2, ui.roomsOnFloor.getValue(2).first().students.size)
    }

    @Test
    fun `available places never goes negative when over-occupied`() {
        val state = RoomsStore.State(
            roomsOnFloor = mapOf(
                1 to listOf(
                    room(id = 1, floor = 1, number = 11, beds = 1, students = listOf(student(1), student(2))),
                ),
            ),
        )

        val ui = mapper.map(state)

        assertEquals(1, ui.totalPlaces)
        assertEquals(0, ui.availablePlaces)
    }

    @Test
    fun `passes through loading and error flags`() {
        val ui = mapper.map(RoomsStore.State(isLoading = true, isError = false))

        assertTrue(ui.isLoading)
        assertEquals(0, ui.totalPlaces)
        assertTrue(ui.roomsOnFloor.isEmpty())
    }

    @Test
    fun `import confirmation carries both incoming and current-to-be-destroyed counts`() {
        val state = RoomsStore.State(
            roomsOnFloor = mapOf(
                2 to listOf(
                    room(id = 1, floor = 2, number = 21, beds = 4, students = listOf(student(1), student(2))),
                ),
                3 to listOf(
                    room(id = 2, floor = 3, number = 31, beds = 3, students = listOf(student(3))),
                ),
            ),
            importConfirmation = Backup(
                rooms = listOf(
                    BackupRoom(floorNumber = 5, roomNumber = 50, bedsCount = 2, students = emptyList()),
                ),
            ),
        )

        val confirmation = mapper.map(state).importConfirmation

        assertNotNull(confirmation)
        assertEquals(1, confirmation.roomCount)
        assertEquals(0, confirmation.studentCount)
        assertEquals(2, confirmation.currentRoomCount)
        assertEquals(3, confirmation.currentStudentCount)
    }
}
