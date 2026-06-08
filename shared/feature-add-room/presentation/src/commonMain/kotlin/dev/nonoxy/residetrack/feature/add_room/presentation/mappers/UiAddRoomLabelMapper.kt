package dev.nonoxy.residetrack.feature.add_room.presentation.mappers

import dev.icerock.moko.resources.format
import dev.nonoxy.residetrack.common.resources.StringConverter
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomErrorKind
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomSuccessKind
import dev.nonoxy.residetrack.feature.add_room.presentation.models.UiAddRoomLabel
import dev.nonoxy.residetrack.res.MR

internal interface UiAddRoomLabelMapper {
    fun map(item: AddRoomStore.Label): UiAddRoomLabel
}

internal class UiAddRoomLabelMapperImpl(
    private val stringConverter: StringConverter,
) : UiAddRoomLabelMapper {

    override fun map(item: AddRoomStore.Label): UiAddRoomLabel = when (item) {
        AddRoomStore.Label.CloseScreen -> UiAddRoomLabel.CloseScreen
        AddRoomStore.Label.NavigateToManageStudentsDraftRoom -> UiAddRoomLabel.NavigateToManageStudentsDraftRoom
        is AddRoomStore.Label.ShowSuccess -> UiAddRoomLabel.ShowSuccess(message = item.kind.toMessage())
        is AddRoomStore.Label.ShowError -> UiAddRoomLabel.ShowError(message = item.kind.toMessage())
    }

    private fun AddRoomSuccessKind.toMessage(): String = when (this) {
        is AddRoomSuccessKind.RoomCreated ->
            stringConverter.convert(MR.strings.add_room_success_message.format(roomNumber))
    }

    private fun AddRoomErrorKind.toMessage(): String = when (this) {
        AddRoomErrorKind.UnknownError -> stringConverter.convert(MR.strings.add_room_error_unknown)
        AddRoomErrorKind.SaveFailed -> stringConverter.convert(MR.strings.add_room_error_save_failed)
        is AddRoomErrorKind.RoomAlreadyExists ->
            stringConverter.convert(MR.strings.add_room_error_room_already_exists.format(roomNumber, floorNumber))
        AddRoomErrorKind.FloorNumberRequired ->
            stringConverter.convert(MR.strings.add_room_validation_floor_number_required)
        AddRoomErrorKind.RoomNumberRequired ->
            stringConverter.convert(MR.strings.add_room_validation_room_number_required)
        AddRoomErrorKind.BedsCountRequired ->
            stringConverter.convert(MR.strings.add_room_validation_beds_count_required)
    }
}
