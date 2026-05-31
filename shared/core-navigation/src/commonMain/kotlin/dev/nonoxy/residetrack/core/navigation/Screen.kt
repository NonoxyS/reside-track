package dev.nonoxy.residetrack.core.navigation

import kotlinx.serialization.Serializable

interface Screen

@Serializable
data object RoomsRoute : Screen

@Serializable
data object AddRoomRoute : Screen

@Serializable
data class ManageStudentsExistingRoomRoute(val roomId: String) : Screen

@Serializable
data object ManageStudentsDraftRoomRoute : Screen
