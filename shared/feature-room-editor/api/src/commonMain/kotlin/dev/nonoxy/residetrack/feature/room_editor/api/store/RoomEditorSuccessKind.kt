package dev.nonoxy.residetrack.feature.room_editor.api.store

sealed interface RoomEditorSuccessKind {
    data object StudentsSaved : RoomEditorSuccessKind
    data object RoomUpdated : RoomEditorSuccessKind
    data object RoomDeleted : RoomEditorSuccessKind
}
