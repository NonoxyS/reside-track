package dev.nonoxy.feature.rooms.presentation.mappers

import dev.nonoxy.feature.rooms.api.store.RoomsStore
import dev.nonoxy.feature.rooms.presentation.models.UiRoomsState
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap

internal interface UiRoomsStateMapper {
    fun map(item: RoomsStore.State): UiRoomsState
}

internal class UiRoomsStateMapperImpl(
    private val roomMapper: UiRoomMapper
) : UiRoomsStateMapper {

    override fun map(item: RoomsStore.State): UiRoomsState =
        UiRoomsState(
            roomsOnFloor = item.roomsOnFloor
                .mapValues { entry -> entry.value.let(roomMapper::map).toPersistentList() }
                .toPersistentMap()
        )
}
