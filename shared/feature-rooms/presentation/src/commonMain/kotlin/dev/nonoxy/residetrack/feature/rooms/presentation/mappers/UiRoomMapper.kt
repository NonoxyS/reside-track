package dev.nonoxy.residetrack.feature.rooms.presentation.mappers

import dev.nonoxy.residetrack.common.utils.mapper.Mapper
import dev.nonoxy.residetrack.core.rooms.models.Room
import dev.nonoxy.residetrack.feature.rooms.presentation.models.UiRoom
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
