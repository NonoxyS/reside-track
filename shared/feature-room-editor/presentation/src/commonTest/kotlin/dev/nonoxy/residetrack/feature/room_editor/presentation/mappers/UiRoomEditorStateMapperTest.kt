package dev.nonoxy.residetrack.feature.room_editor.presentation.mappers

import dev.nonoxy.residetrack.core.rooms.models.Room
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorErrorKind
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore.DateField
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiDateField
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UiRoomEditorStateMapperTest {

    private val mapper: UiRoomEditorStateMapper =
        UiRoomEditorStateMapperImpl(uiRoomMapper = UiRoomMapperImpl())

    @Test
    fun `maps room students picker params and flags`() {
        val state = RoomEditorStore.State(
            room = Room(id = 1, floorNumber = 2, roomNumber = 21, bedsCount = 4, students = emptyList()),
            editableStudents = persistentListOf(
                RoomEditorStore.State.EditableStudent(
                    id = "uuid-1",
                    studentId = 10L,
                    streamNumber = "305",
                    checkInDate = "01.01.2030",
                    checkOutDate = "01.06.2030",
                    isNew = false,
                ),
            ),
            openDatePicker = RoomEditorStore.State.OpenPicker(studentId = "uuid-1", field = DateField.CHECK_OUT),
            showRoomParams = true,
            roomParams = RoomEditorStore.State.RoomParams(
                floorNumber = "2",
                roomNumber = "21",
                bedsCount = "4",
                roomNumberError = true,
            ),
            showDeleteConfirm = true,
            removingStudentId = "uuid-1",
        )

        val ui = mapper.map(state)

        assertEquals("2", ui.room?.floorNumber)
        assertEquals("21", ui.room?.roomNumber)
        assertEquals(1, ui.editableStudents.size)
        assertEquals("305", ui.editableStudents.first().streamNumber)
        assertEquals(UiDateField.CHECK_OUT, ui.openDatePicker?.field)
        assertEquals("uuid-1", ui.openDatePicker?.studentId)
        assertTrue(ui.showRoomParams)
        assertTrue(ui.roomParams?.roomNumberError == true)
        assertTrue(ui.showDeleteConfirm)
        assertEquals("uuid-1", ui.removingStudentId)
    }

    @Test
    fun `maps error state with a null room`() {
        val state = RoomEditorStore.State(
            isError = true,
            errorKind = RoomEditorErrorKind.RoomNotFound,
            room = null,
        )

        val ui = mapper.map(state)

        assertTrue(ui.isError)
        assertEquals(RoomEditorErrorKind.RoomNotFound, ui.errorKind)
        assertNull(ui.room)
    }
}
