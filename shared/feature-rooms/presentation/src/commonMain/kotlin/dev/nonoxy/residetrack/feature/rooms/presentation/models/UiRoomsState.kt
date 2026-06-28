package dev.nonoxy.residetrack.feature.rooms.presentation.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

data class UiRoomsState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val totalPlaces: Int = 0,
    val availablePlaces: Int = 0,
    val roomsOnFloor: ImmutableMap<Int, ImmutableList<UiRoom>> = persistentMapOf(),
    val importConfirmation: UiImportConfirmation? = null,
)
