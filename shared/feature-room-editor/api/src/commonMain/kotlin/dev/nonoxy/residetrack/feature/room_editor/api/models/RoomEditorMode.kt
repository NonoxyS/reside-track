package dev.nonoxy.residetrack.feature.room_editor.api.models

sealed interface RoomEditorMode {
    data class ExistingRoom(val roomId: String) : RoomEditorMode
    data object DraftRoom : RoomEditorMode
}
