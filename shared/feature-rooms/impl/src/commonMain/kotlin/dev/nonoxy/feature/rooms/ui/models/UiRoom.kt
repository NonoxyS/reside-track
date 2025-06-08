package dev.nonoxy.feature.rooms.ui.models

import kotlinx.collections.immutable.ImmutableList

internal data class UiRoom(
    val id: Long,
    val floorNumber: String,
    val roomNumber: String,
    val bedsCount: String,
    val students: ImmutableList<UiStudent>
)
