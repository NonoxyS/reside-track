package dev.nonoxy.residetrack.feature.upcoming.presentation.models

sealed interface UiUpcomingLabel {
    data class NavigateToRoomEditorExistingRoom(val roomId: String) : UiUpcomingLabel
}
