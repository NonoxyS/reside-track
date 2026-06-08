package dev.nonoxy.residetrack.feature.add_room.ui

import androidx.compose.runtime.Composable
import dev.icerock.moko.resources.compose.stringResource
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomErrorKind
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
