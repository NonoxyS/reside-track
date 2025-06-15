package dev.nonoxy.feature.rooms.presentation.models

import dev.nonoxy.feature.rooms.ui.models.UiRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

internal data class RoomsViewState(
    val tabsState: RoomsTabsState,
    val allRooms: ImmutableList<UiRoom>,
    val roomsOnFloor: ImmutableMap<Int, ImmutableList<UiRoom>>,
    val selectedFloorTotalBeds: Int,
    val selectedFloorAvailableBeds: Int
) {
    companion object {
        val Initial: RoomsViewState = RoomsViewState(
            tabsState = RoomsTabsState.Initial,
            allRooms = persistentListOf(),
            roomsOnFloor = persistentMapOf(),
            selectedFloorTotalBeds = 0,
            selectedFloorAvailableBeds = 0
        )
    }
}

internal data class RoomsTabsState(
    val selectedTabIndex: Int,
    val selectedFloor: Int?,
) {
    companion object {
        val Initial: RoomsTabsState = RoomsTabsState(
            selectedTabIndex = 0,
            selectedFloor = null,
        )
    }
}
