package dev.nonoxy.feature.add_room.presentation.models

import dev.nonoxy.feature.add_room.api.store.AddRoomErrorKind
import dev.nonoxy.feature.add_room.api.store.AddRoomSuccessKind

sealed interface UiAddRoomLabel {
    data object CloseScreen : UiAddRoomLabel
    data object NavigateToManageStudentsDraftRoom : UiAddRoomLabel
    data class ShowSuccess(val kind: AddRoomSuccessKind) : UiAddRoomLabel
    data class ShowError(val kind: AddRoomErrorKind) : UiAddRoomLabel
}
