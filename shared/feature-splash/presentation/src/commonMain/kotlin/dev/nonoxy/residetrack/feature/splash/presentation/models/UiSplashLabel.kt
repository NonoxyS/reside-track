package dev.nonoxy.residetrack.feature.splash.presentation.models

sealed interface UiSplashLabel {
    data object NavigateToRooms : UiSplashLabel
}
