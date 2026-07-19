package dev.nonoxy.residetrack.feature.room_editor.presentation.mappers

import dev.nonoxy.residetrack.common.resources.StringConverter
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorErrorKind
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiRoomEditorLabel
import dev.nonoxy.residetrack.res.MR

internal interface UiRoomEditorLabelMapper {
    fun map(item: RoomEditorStore.Label): UiRoomEditorLabel?
}

internal class UiRoomEditorLabelMapperImpl(
    private val stringConverter: StringConverter,
) : UiRoomEditorLabelMapper {

    override fun map(item: RoomEditorStore.Label): UiRoomEditorLabel? = when (item) {
        RoomEditorStore.Label.NavigateBack -> UiRoomEditorLabel.NavigateBack
        is RoomEditorStore.Label.ShowError -> UiRoomEditorLabel.ShowError(message = item.kind.toMessage())
        is RoomEditorStore.Label.ShowSuccess -> null
    }

    private fun RoomEditorErrorKind.toMessage(): String = when (this) {
        RoomEditorErrorKind.FailedToLoadStudents -> stringConverter.convert(
            MR.strings.error_failed_to_load_students
        )
        RoomEditorErrorKind.FailedToSaveStudents -> stringConverter.convert(
            MR.strings.error_failed_to_save_students
        )
        RoomEditorErrorKind.RoomNotFound -> stringConverter.convert(MR.strings.error_room_not_found)
        RoomEditorErrorKind.DraftRoomNotFound -> stringConverter.convert(MR.strings.error_draft_room_not_found)
        RoomEditorErrorKind.IncompleteStudentData ->
            stringConverter.convert(MR.strings.error_student_fields_required)
        RoomEditorErrorKind.StreamNumberInvalid -> stringConverter.convert(MR.strings.error_stream_number_invalid)
        RoomEditorErrorKind.InvalidDateRange -> stringConverter.convert(MR.strings.error_invalid_date_range)
        RoomEditorErrorKind.InvalidDateFormat -> stringConverter.convert(MR.strings.error_invalid_date_format)
        RoomEditorErrorKind.DuplicateStreamNumbers ->
            stringConverter.convert(MR.strings.error_duplicate_stream_numbers)
        RoomEditorErrorKind.RoomNumberTaken -> stringConverter.convert(MR.strings.error_room_number_taken)
        RoomEditorErrorKind.FailedToUpdateRoom -> stringConverter.convert(MR.strings.error_failed_to_update_room)
        RoomEditorErrorKind.FailedToDeleteRoom -> stringConverter.convert(MR.strings.error_failed_to_delete_room)
    }
}
