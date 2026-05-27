package dev.nonoxy.feature.manage_students.presentation.mappers

import dev.nonoxy.common.utils.mapper.Mapper
import dev.nonoxy.feature.manage_students.presentation.models.UiRoom
import dev.nonoxy.feature.rooms.api.models.Room
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
