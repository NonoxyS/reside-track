package dev.nonoxy.residetrack.feature.add_room.presentation.models

import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomErrorKind
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomSuccessKind

sealed interface UiAddRoomLabel {
    data object CloseScreen : UiAddRoomLabel
    data object NavigateToManageStudentsDraftRoom : UiAddRoomLabel
    data class ShowSuccess(val kind: AddRoomSuccessKind) : UiAddRoomLabel
    data class ShowError(val kind: AddRoomErrorKind) : UiAddRoomLabel
}
