package dev.nonoxy.feature.rooms.data.mappers

import dev.nonoxy.common.utils.mapper.Mapper
import dev.nonoxy.core.database.entities.RoomEntity
import dev.nonoxy.core.database.relations.RoomWithStudents
import dev.nonoxy.feature.rooms.models.Room

internal interface RoomMapper : Mapper<RoomWithStudents, Room> {
    fun map(item: Room): RoomEntity
}

internal class RoomMapperImpl(
    private val studentMapper: StudentMapper
) : RoomMapper {

    override fun map(item: RoomWithStudents): Room = with(item) {
        Room(
            id = room.id,
            floorNumber = room.floorNumber,
            roomNumber = room.roomNumber,
            bedsCount = room.bedsCount,
            students = studentMapper.map(list = students)
        )
    }

    override fun map(item: Room): RoomEntity = with(item) {
        RoomEntity(
            floorNumber = floorNumber,
            roomNumber = roomNumber,
            bedsCount = bedsCount
        )
    }
}
