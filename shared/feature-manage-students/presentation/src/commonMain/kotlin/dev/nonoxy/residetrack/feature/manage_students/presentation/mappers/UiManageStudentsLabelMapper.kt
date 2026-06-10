package dev.nonoxy.residetrack.feature.manage_students.presentation.mappers

import dev.nonoxy.residetrack.common.resources.StringConverter
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsErrorKind
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsSuccessKind
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiManageStudentsLabel
import dev.nonoxy.residetrack.res.MR

internal interface UiManageStudentsLabelMapper {
    fun map(item: ManageStudentsStore.Label): UiManageStudentsLabel
}

internal class UiManageStudentsLabelMapperImpl(
    private val stringConverter: StringConverter,
) : UiManageStudentsLabelMapper {

    override fun map(item: ManageStudentsStore.Label): UiManageStudentsLabel = when (item) {
        ManageStudentsStore.Label.NavigateBack -> UiManageStudentsLabel.NavigateBack
        is ManageStudentsStore.Label.ShowError -> UiManageStudentsLabel.ShowError(message = item.kind.toMessage())
        is ManageStudentsStore.Label.ShowSuccess -> UiManageStudentsLabel.ShowSuccess(message = item.kind.toMessage())
    }

    private fun ManageStudentsErrorKind.toMessage(): String = when (this) {
        ManageStudentsErrorKind.FailedToLoadStudents -> stringConverter.convert(
            MR.strings.error_failed_to_load_students
        )
        ManageStudentsErrorKind.FailedToSaveStudents -> stringConverter.convert(
            MR.strings.error_failed_to_save_students
        )
        ManageStudentsErrorKind.RoomNotFound -> stringConverter.convert(MR.strings.error_room_not_found)
        ManageStudentsErrorKind.DraftRoomNotFound -> stringConverter.convert(MR.strings.error_draft_room_not_found)
        ManageStudentsErrorKind.StreamNumberInvalid -> stringConverter.convert(MR.strings.error_stream_number_invalid)
        ManageStudentsErrorKind.InvalidDateRange -> stringConverter.convert(MR.strings.error_invalid_date_range)
        ManageStudentsErrorKind.InvalidDateFormat -> stringConverter.convert(MR.strings.error_invalid_date_format)
        ManageStudentsErrorKind.DuplicateStreamNumbers ->
            stringConverter.convert(MR.strings.error_duplicate_stream_numbers)
        ManageStudentsErrorKind.RoomNumberTaken -> stringConverter.convert(MR.strings.error_room_number_taken)
        ManageStudentsErrorKind.FailedToUpdateRoom -> stringConverter.convert(MR.strings.error_failed_to_update_room)
        ManageStudentsErrorKind.FailedToDeleteRoom -> stringConverter.convert(MR.strings.error_failed_to_delete_room)
    }

    private fun ManageStudentsSuccessKind.toMessage(): String = when (this) {
        ManageStudentsSuccessKind.StudentsSaved -> stringConverter.convert(MR.strings.students_saved_successfully)
        ManageStudentsSuccessKind.RoomUpdated -> stringConverter.convert(MR.strings.room_updated_successfully)
        ManageStudentsSuccessKind.RoomDeleted -> stringConverter.convert(MR.strings.room_deleted_successfully)
    }
}
