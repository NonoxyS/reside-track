package dev.nonoxy.residetrack.feature.room_editor.api.store

sealed interface RoomEditorErrorKind {
    data object FailedToLoadStudents : RoomEditorErrorKind
    data object FailedToSaveStudents : RoomEditorErrorKind
    data object RoomNotFound : RoomEditorErrorKind
    data object DraftRoomNotFound : RoomEditorErrorKind
    data object StreamNumberInvalid : RoomEditorErrorKind
    data object InvalidDateRange : RoomEditorErrorKind
    data object InvalidDateFormat : RoomEditorErrorKind
    data object DuplicateStreamNumbers : RoomEditorErrorKind
    data object RoomNumberTaken : RoomEditorErrorKind
    data object FailedToUpdateRoom : RoomEditorErrorKind
    data object FailedToDeleteRoom : RoomEditorErrorKind
}
