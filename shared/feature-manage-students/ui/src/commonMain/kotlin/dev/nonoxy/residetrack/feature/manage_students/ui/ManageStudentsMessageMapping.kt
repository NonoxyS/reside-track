package dev.nonoxy.residetrack.feature.manage_students.ui

import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsErrorKind
import dev.nonoxy.residetrack.feature.manage_students.api.store.ManageStudentsSuccessKind
import dev.nonoxy.residetrack.common.resources.StringConverter
import dev.nonoxy.residetrack.res.MR

internal fun ManageStudentsErrorKind.localized(converter: StringConverter): String = when (this) {
    ManageStudentsErrorKind.FailedToLoadStudents -> converter.convert(MR.strings.error_failed_to_load_students)
    ManageStudentsErrorKind.FailedToSaveStudents -> converter.convert(MR.strings.error_failed_to_save_students)
    ManageStudentsErrorKind.RoomNotFound -> converter.convert(MR.strings.error_room_not_found)
    ManageStudentsErrorKind.DraftRoomNotFound -> converter.convert(MR.strings.error_draft_room_not_found)
    ManageStudentsErrorKind.StreamNumberInvalid -> converter.convert(MR.strings.error_stream_number_invalid)
    ManageStudentsErrorKind.InvalidDateRange -> converter.convert(MR.strings.error_invalid_date_range)
    ManageStudentsErrorKind.InvalidDateFormat -> converter.convert(MR.strings.error_invalid_date_format)
    ManageStudentsErrorKind.DuplicateStreamNumbers -> converter.convert(MR.strings.error_duplicate_stream_numbers)
}

internal fun ManageStudentsSuccessKind.localized(converter: StringConverter): String = when (this) {
    ManageStudentsSuccessKind.StudentsSaved -> converter.convert(MR.strings.students_saved_successfully)
}
