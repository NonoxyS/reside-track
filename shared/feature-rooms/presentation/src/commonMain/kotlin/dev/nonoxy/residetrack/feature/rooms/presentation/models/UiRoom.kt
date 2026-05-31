package dev.nonoxy.residetrack.feature.rooms.presentation.models

import kotlinx.collections.immutable.ImmutableList

data class UiRoom(
    val id: Long,
    val floorNumber: String,
    val roomNumber: String,
    val bedsCount: String,
    val students: ImmutableList<UiStudent>
)
