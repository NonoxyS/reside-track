package dev.nonoxy.residetrack.feature.rooms.presentation.mappers

import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore
import dev.nonoxy.residetrack.feature.rooms.presentation.models.UiRoomsState
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap

internal interface UiRoomsStateMapper {
    fun map(item: RoomsStore.State): UiRoomsState
}

internal class UiRoomsStateMapperImpl(
    private val roomMapper: UiRoomMapper
) : UiRoomsStateMapper {

    override fun map(item: RoomsStore.State): UiRoomsState {
        val allRooms = item.roomsOnFloor.values.flatten()
        val totalPlaces = allRooms.sumOf { room -> room.bedsCount }
        val occupiedPlaces = allRooms.sumOf { room -> room.students.size }

        return UiRoomsState(
            isLoading = item.isLoading,
            isError = item.isError,
            totalPlaces = totalPlaces,
            availablePlaces = (totalPlaces - occupiedPlaces).coerceAtLeast(0),
            roomsOnFloor = item.roomsOnFloor
                .mapValues { entry -> entry.value.let(roomMapper::map).toPersistentList() }
                .toPersistentMap()
        )
    }
}
