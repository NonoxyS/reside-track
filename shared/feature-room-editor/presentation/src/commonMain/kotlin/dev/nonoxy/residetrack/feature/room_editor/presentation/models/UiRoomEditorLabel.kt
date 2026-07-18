package dev.nonoxy.residetrack.feature.room_editor.presentation.models

sealed interface UiRoomEditorLabel {
    data object NavigateBack : UiRoomEditorLabel
    data class ShowError(val message: String) : UiRoomEditorLabel
}
