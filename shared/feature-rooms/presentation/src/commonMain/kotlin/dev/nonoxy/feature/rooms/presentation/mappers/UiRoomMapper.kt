package dev.nonoxy.feature.rooms.presentation.mappers

import dev.nonoxy.common.utils.mapper.Mapper
import dev.nonoxy.feature.rooms.api.models.Room
import dev.nonoxy.feature.rooms.presentation.models.UiRoom
import kotlinx.collections.immutable.toImmutableList

// TODO Task 7: restore `internal` on both interface and Impl — currently public so OldRoomsViewModel
//  in :shared:feature-rooms:impl can construct UiRoomMapperImpl via Koin DSL `::Impl` constructor ref.
interface UiRoomMapper : Mapper<Room, UiRoom>

class UiRoomMapperImpl(
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
