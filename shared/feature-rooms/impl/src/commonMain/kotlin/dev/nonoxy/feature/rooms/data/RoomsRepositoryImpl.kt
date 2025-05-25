package dev.nonoxy.feature.rooms.data

import dev.nonoxy.common.utils.coRunCatching
import dev.nonoxy.common.utils.wrapFailure
import dev.nonoxy.common.utils.wrapSuccess
import dev.nonoxy.core.database.dao.RoomDao
import dev.nonoxy.core.database.entities.RoomEntity
import dev.nonoxy.core.database.relations.RoomWithStudents
import dev.nonoxy.feature.rooms.data.mappers.RoomMapper
import dev.nonoxy.feature.rooms.models.Room
import dev.nonoxy.feature.rooms.repository.RoomsRepository

internal class RoomsRepositoryImpl(
    private val roomDao: RoomDao,
    private val roomMapper: RoomMapper,
) : RoomsRepository {

    override suspend fun getAllRooms(): Result<List<Room>> {
        return coRunCatching(
            tryBlock = {
                roomDao.getAllRoomsWithStudents()
                    .mapToDomain()
                    .wrapSuccess()
            },
            catchBlock = { throwable -> throwable.wrapFailure() }
        )
    }

    override suspend fun getRoomsByFloor(floorNumber: Int): Result<List<Room>> {
        return coRunCatching(
            tryBlock = {
                roomDao.getRoomsWithStudentsByFloor(floor = floorNumber)
                    .mapToDomain()
                    .wrapSuccess()
            },
            catchBlock = { throwable -> throwable.wrapFailure() }
        )
    }

    override suspend fun getRoomByNumber(roomNumber: Int): Result<Room?> {
        return coRunCatching(
            tryBlock = {
                roomDao.getRoomWithStudentsByRoomNumber(roomNumber = roomNumber)
                    ?.mapToDomain()
                    .wrapSuccess()
            },
            catchBlock = { throwable -> throwable.wrapFailure() }
        )
    }

    override suspend fun getRoomById(roomId: Long): Result<Room?> {
        return coRunCatching(
            tryBlock = {
                roomDao.getRoomWithStudentsById(roomId = roomId)
                    ?.mapToDomain()
                    .wrapSuccess()
            },
            catchBlock = { throwable -> throwable.wrapFailure() }
        )
    }

    override suspend fun saveRoom(room: Room): Result<Unit> {
        return coRunCatching(
            tryBlock = {
                roomDao.insertRoom(room = room.mapToEntity()).wrapSuccess()
            },
            catchBlock = { throwable -> throwable.wrapFailure() }
        )
    }

    private fun RoomWithStudents.mapToDomain(): Room = roomMapper.map(this)
    private fun List<RoomWithStudents>.mapToDomain(): List<Room> = map { it.mapToDomain() }
    private fun Room.mapToEntity(): RoomEntity = roomMapper.map(this)
}
