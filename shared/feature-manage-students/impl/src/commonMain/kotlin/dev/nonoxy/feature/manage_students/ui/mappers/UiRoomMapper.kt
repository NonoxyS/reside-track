package dev.nonoxy.feature.manage_students.ui.mappers

import dev.nonoxy.common.utils.mapper.Mapper
import dev.nonoxy.feature.manage_students.ui.models.UiRoom
import dev.nonoxy.feature.rooms.models.Room
import kotlinx.collections.immutable.toImmutableList

internal interface UiRoomMapper : Mapper<Room, UiRoom>

internal class UiRoomMapperImpl(
    private val studentMapper: UiStudentMapper
) : UiRoomMapper {

    override fun map(item: Room): UiRoom = with(item) {
        return UiRoom(
            id = id,
            floorNumber = floorNumber.toString(),
            roomNumber = roomNumber.toString(),
            bedsCount = bedsCount.toString(),
            students = students.let(studentMapper::map).toImmutableList()
        )
    }
}
