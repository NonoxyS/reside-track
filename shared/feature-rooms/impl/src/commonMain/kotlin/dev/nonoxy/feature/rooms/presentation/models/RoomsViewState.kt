package dev.nonoxy.feature.rooms.presentation.models

data class RoomsViewState(
    val rooms: Any
) {
    companion object {
        val Initial: RoomsViewState = RoomsViewState(
            data = 1
        )
    }
}
