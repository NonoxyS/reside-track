package dev.nonoxy.feature.rooms.ui.models

import kotlinx.collections.immutable.ImmutableList

data class UiRoom(
    val floorNumber: String,
    val roomNumber: String,
    val bedsCount: String,
    val students: ImmutableList<UiStudent>
)
