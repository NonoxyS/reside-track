package dev.nonoxy.feature.rooms.ui.mappers

import dev.nonoxy.common.utils.mapper.Mapper
import dev.nonoxy.feature.rooms.models.Room
import dev.nonoxy.feature.rooms.ui.models.UiRoom
import kotlinx.collections.immutable.toImmutableList

internal interface UiRoomMapper : Mapper<Room, UiRoom>

internal class UiRoomMapperImpl(
    private val studentMapper: UiStudentMapper
) : UiRoomMapper {

    override fun map(item: Room): UiRoom = with(item) {
        return UiRoom(
            floorNumber = floorNumber.toString(),
            roomNumber = roomNumber.toString(),
            bedsCount = bedsCount.toString(),
            students = students.let(studentMapper::map).toImmutableList()
        )
    }
}
