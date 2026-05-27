package dev.nonoxy.feature.manage_students.ui

import dev.nonoxy.feature.manage_students.api.store.ManageStudentsErrorKind
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsSuccessKind
import org.jetbrains.compose.resources.getString
import residetrack.shared.feature_manage_students.ui.generated.resources.Res
import residetrack.shared.feature_manage_students.ui.generated.resources.error_draft_room_not_found
import residetrack.shared.feature_manage_students.ui.generated.resources.error_duplicate_stream_numbers
import residetrack.shared.feature_manage_students.ui.generated.resources.error_failed_to_load_students
import residetrack.shared.feature_manage_students.ui.generated.resources.error_failed_to_save_students
import residetrack.shared.feature_manage_students.ui.generated.resources.error_invalid_date_format
import residetrack.shared.feature_manage_students.ui.generated.resources.error_invalid_date_range
import residetrack.shared.feature_manage_students.ui.generated.resources.error_room_not_found
import residetrack.shared.feature_manage_students.ui.generated.resources.error_stream_number_invalid
import residetrack.shared.feature_manage_students.ui.generated.resources.students_saved_successfully

internal suspend fun ManageStudentsErrorKind.localizedSuspend(): String = when (this) {
    ManageStudentsErrorKind.FailedToLoadStudents -> getString(Res.string.error_failed_to_load_students)
    ManageStudentsErrorKind.FailedToSaveStudents -> getString(Res.string.error_failed_to_save_students)
    ManageStudentsErrorKind.RoomNotFound -> getString(Res.string.error_room_not_found)
    ManageStudentsErrorKind.DraftRoomNotFound -> getString(Res.string.error_draft_room_not_found)
    ManageStudentsErrorKind.StreamNumberInvalid -> getString(Res.string.error_stream_number_invalid)
    ManageStudentsErrorKind.InvalidDateRange -> getString(Res.string.error_invalid_date_range)
    ManageStudentsErrorKind.InvalidDateFormat -> getString(Res.string.error_invalid_date_format)
    ManageStudentsErrorKind.DuplicateStreamNumbers -> getString(Res.string.error_duplicate_stream_numbers)
}

internal suspend fun ManageStudentsSuccessKind.localizedSuspend(): String = when (this) {
    ManageStudentsSuccessKind.StudentsSaved -> getString(Res.string.students_saved_successfully)
}
