package dev.nonoxy.residetrack.core.rooms.data.mappers

import dev.nonoxy.residetrack.core.rooms.domain.model.Room
import kotlin.test.Test
import kotlin.test.assertEquals

class RoomMapperImplTest {

    private val mapper = RoomMapperImpl(studentMapper = StudentMapperImpl())

    @Test
    fun `map Room to entity preserves id so an existing room updates in place instead of inserting a duplicate`() {
        val room = Room(id = 42, floorNumber = 3, roomNumber = 333, bedsCount = 6, students = emptyList())

        val entity = mapper.map(room)

        assertEquals(42, entity.id)
        assertEquals(3, entity.floorNumber)
        assertEquals(333, entity.roomNumber)
        assertEquals(6, entity.bedsCount)
    }
}
