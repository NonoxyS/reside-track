package dev.nonoxy.residetrack.feature.rooms.presentation.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

data class UiRoomsState(
    val roomsOnFloor: ImmutableMap<Int, ImmutableList<UiRoom>> = persistentMapOf(),
)
