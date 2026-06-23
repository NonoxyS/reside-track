package dev.nonoxy.residetrack.feature.add_room.presentation.mappers

import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomErrorKind
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiAddRoomStateMapperTest {

    private val mapper: UiAddRoomStateMapper = UiAddRoomStateMapperImpl()

    @Test
    fun `maps every field including nested selections and dirty flag`() {
        val state = AddRoomStore.State(
            floorSelection = AddRoomStore.State.FloorSelectionState(
                textField = AddRoomStore.State.TextFieldState(value = "3"),
                existingFloors = persistentListOf(1, 2, 3),
                showInput = true,
            ),
            roomNumber = AddRoomStore.State.TextFieldState(
                value = "21",
                errorKind = AddRoomErrorKind.RoomNumberRequired,
            ),
            bedsSelection = AddRoomStore.State.BedsSelectionState(
                textField = AddRoomStore.State.TextFieldState(value = "4"),
                existingBedsCounts = persistentListOf(2, 4),
                showInput = false,
            ),
            isLoading = true,
            isFormValid = true,
            hasExistingRooms = true,
            showDiscardConfirm = true,
        )

        val ui = mapper.map(state)

        assertEquals("3", ui.floorSelection.textField.value)
        assertEquals(persistentListOf(1, 2, 3), ui.floorSelection.existingFloors)
        assertTrue(ui.floorSelection.showInput)
        assertEquals("21", ui.roomNumber.value)
        assertEquals(AddRoomErrorKind.RoomNumberRequired, ui.roomNumber.errorKind)
        assertEquals(persistentListOf(2, 4), ui.bedsSelection.existingBedsCounts)
        assertTrue(ui.isLoading)
        assertTrue(ui.isFormValid)
        assertTrue(ui.hasExistingRooms)
        assertTrue(ui.showDiscardConfirm)
        assertTrue(ui.isDirty) // derived: floor + room + beds all non-blank
    }

    @Test
    fun `empty state maps to a non-dirty form`() {
        val ui = mapper.map(AddRoomStore.State())

        assertEquals("", ui.floorSelection.textField.value)
        assertTrue(ui.floorSelection.existingFloors.isEmpty())
        assertEquals(false, ui.isDirty)
    }
}
