package dev.nonoxy.residetrack.feature.upcoming.presentation.models

sealed interface UiUpcomingLabel {
    data class NavigateToManageStudentsExistingRoom(val roomId: String) : UiUpcomingLabel
}
