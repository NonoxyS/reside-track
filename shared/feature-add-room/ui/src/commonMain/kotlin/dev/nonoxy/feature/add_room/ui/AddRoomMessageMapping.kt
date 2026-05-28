package dev.nonoxy.feature.add_room.ui

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.format
import dev.nonoxy.feature.add_room.api.store.AddRoomErrorKind
import dev.nonoxy.feature.add_room.api.store.AddRoomSuccessKind
import dev.nonoxy.residetrack.common.resources.StringConverter
import dev.nonoxy.residetrack.res.MR

/** For inline error rendering inside @Composable views (TextField supportingText etc.). */
@Composable
internal fun AddRoomErrorKind.localized(): String = when (this) {
    AddRoomErrorKind.UnknownError -> stringResource(MR.strings.add_room_error_unknown)
    AddRoomErrorKind.SaveFailed -> stringResource(MR.strings.add_room_error_save_failed)
    is AddRoomErrorKind.RoomAlreadyExists ->
        stringResource(MR.strings.add_room_error_room_already_exists, roomNumber, floorNumber)
    AddRoomErrorKind.FloorNumberRequired -> stringResource(MR.strings.add_room_validation_floor_number_required)
    AddRoomErrorKind.RoomNumberRequired -> stringResource(MR.strings.add_room_validation_room_number_required)
    AddRoomErrorKind.BedsCountRequired -> stringResource(MR.strings.add_room_validation_beds_count_required)
}

/** For one-shot snackbars triggered from CollectFlow lambdas (non-Composable scope). */
internal fun AddRoomErrorKind.localized(converter: StringConverter): String = when (this) {
    AddRoomErrorKind.UnknownError -> converter.convert(MR.strings.add_room_error_unknown)
    AddRoomErrorKind.SaveFailed -> converter.convert(MR.strings.add_room_error_save_failed)
    is AddRoomErrorKind.RoomAlreadyExists ->
        converter.convert(MR.strings.add_room_error_room_already_exists.format(roomNumber, floorNumber))
    AddRoomErrorKind.FloorNumberRequired ->
        converter.convert(MR.strings.add_room_validation_floor_number_required)
    AddRoomErrorKind.RoomNumberRequired ->
        converter.convert(MR.strings.add_room_validation_room_number_required)
    AddRoomErrorKind.BedsCountRequired ->
        converter.convert(MR.strings.add_room_validation_beds_count_required)
}

internal fun AddRoomSuccessKind.localized(converter: StringConverter): String = when (this) {
    is AddRoomSuccessKind.RoomCreated ->
        converter.convert(MR.strings.add_room_success_message.format(roomNumber))
}
