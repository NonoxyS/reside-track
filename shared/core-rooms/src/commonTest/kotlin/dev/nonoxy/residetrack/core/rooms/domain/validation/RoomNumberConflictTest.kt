package dev.nonoxy.residetrack.core.rooms.domain.validation

import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RoomNumberConflictTest {

    private fun room(id: Long, floor: Int, number: Int) =
        Room(id = id, floorNumber = floor, roomNumber = number, bedsCount = 2, students = emptyList())

    private val existing = listOf(
        room(1, floor = 3, number = 301),
        room(2, floor = 3, number = 302),
        room(3, floor = 4, number = 301),
    )

    @Test
    fun conflict_whenSameFloorAndNumber_andNotSelf() {
        assertTrue(
            RoomNumberConflict.exists(existing, floorNumber = 3, roomNumber = 301, excludeRoomId = null)
        )
    }

    @Test
    fun noConflict_whenDifferentFloor() {
        assertFalse(
            RoomNumberConflict.exists(existing, floorNumber = 5, roomNumber = 301, excludeRoomId = null)
        )
    }

    @Test
    fun noConflict_whenSameNumberDifferentFloor() {
        // 301 exists on floor 3 and 4, but not floor 2
        assertFalse(
            RoomNumberConflict.exists(existing, floorNumber = 2, roomNumber = 301, excludeRoomId = null)
        )
    }

    @Test
    fun noConflict_whenMatchIsSelf() {
        // editing room id=1 (floor 3 / 301) and keeping its own number must NOT conflict
        assertFalse(
            RoomNumberConflict.exists(existing, floorNumber = 3, roomNumber = 301, excludeRoomId = 1)
        )
    }

    @Test
    fun conflict_whenMovingOntoAnotherRoomsNumber_evenWithExcludeSelf() {
        // editing room id=1 but changing to 302 (taken by id=2) must conflict
        assertTrue(
            RoomNumberConflict.exists(existing, floorNumber = 3, roomNumber = 302, excludeRoomId = 1)
        )
    }
}
