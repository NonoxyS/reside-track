package dev.nonoxy.feature.add_room.ui

import androidx.compose.runtime.Composable
import dev.nonoxy.feature.add_room.api.store.AddRoomErrorKind
import dev.nonoxy.feature.add_room.api.store.AddRoomSuccessKind
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import residetrack.shared.feature_add_room.ui.generated.resources.Res
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_error_room_already_exists
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_error_save_failed
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_error_unknown
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_success_message
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_validation_beds_count_required
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_validation_floor_number_required
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_validation_room_number_required

/** For inline error rendering inside @Composable views (TextField supportingText etc.). */
@Composable
internal fun AddRoomErrorKind.localized(): String = when (this) {
    AddRoomErrorKind.UnknownError -> stringResource(Res.string.add_room_error_unknown)
    AddRoomErrorKind.SaveFailed -> stringResource(Res.string.add_room_error_save_failed)
    is AddRoomErrorKind.RoomAlreadyExists ->
        stringResource(Res.string.add_room_error_room_already_exists, roomNumber, floorNumber)
    AddRoomErrorKind.FloorNumberRequired -> stringResource(Res.string.add_room_validation_floor_number_required)
    AddRoomErrorKind.RoomNumberRequired -> stringResource(Res.string.add_room_validation_room_number_required)
    AddRoomErrorKind.BedsCountRequired -> stringResource(Res.string.add_room_validation_beds_count_required)
}

/** For one-shot snackbars triggered from CollectFlow lambdas (suspend, non-Composable scope). */
internal suspend fun AddRoomErrorKind.localizedSuspend(): String = when (this) {
    AddRoomErrorKind.UnknownError -> getString(Res.string.add_room_error_unknown)
    AddRoomErrorKind.SaveFailed -> getString(Res.string.add_room_error_save_failed)
    is AddRoomErrorKind.RoomAlreadyExists ->
        getString(Res.string.add_room_error_room_already_exists, roomNumber, floorNumber)
    AddRoomErrorKind.FloorNumberRequired -> getString(Res.string.add_room_validation_floor_number_required)
    AddRoomErrorKind.RoomNumberRequired -> getString(Res.string.add_room_validation_room_number_required)
    AddRoomErrorKind.BedsCountRequired -> getString(Res.string.add_room_validation_beds_count_required)
}

internal suspend fun AddRoomSuccessKind.localizedSuspend(): String = when (this) {
    is AddRoomSuccessKind.RoomCreated -> getString(Res.string.add_room_success_message, roomNumber)
}
