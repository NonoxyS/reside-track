package dev.nonoxy.residetrack.feature.room_editor.presentation.mappers

import dev.nonoxy.residetrack.common.utils.mapper.Mapper
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiRoom
import dev.nonoxy.residetrack.core.rooms.models.Room
import kotlinx.collections.immutable.toImmutableList

interface UiRoomMapper : Mapper<Room, UiRoom>

class UiRoomMapperImpl(
    private val studentMapper: UiStudentMapper,
) : UiRoomMapper {

    override fun map(item: Room): UiRoom = with(item) {
        UiRoom(
            id = id,
            floorNumber = floorNumber.toString(),
            roomNumber = roomNumber.toString(),
            bedsCount = bedsCount.toString(),
            students = students.let(studentMapper::map).toImmutableList(),
        )
    }
}
