package dev.nonoxy.core.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {

    @Serializable
    data object Rooms : Screen

    @Serializable
    data class RoomDetail(val roomNumber: Long) : Screen

    @Serializable
    data object AddRoom : Screen
}
