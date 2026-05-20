package dev.nonoxy.feature.manage_students.utils

import org.jetbrains.compose.resources.getString
import residetrack.shared.feature_manage_students.impl.generated.resources.Res
import residetrack.shared.feature_manage_students.impl.generated.resources.error_check_in_date_required
import residetrack.shared.feature_manage_students.impl.generated.resources.error_check_out_date_required
import residetrack.shared.feature_manage_students.impl.generated.resources.error_draft_room_not_found
import residetrack.shared.feature_manage_students.impl.generated.resources.error_duplicate_stream_numbers
import residetrack.shared.feature_manage_students.impl.generated.resources.error_failed_to_load_students
import residetrack.shared.feature_manage_students.impl.generated.resources.error_failed_to_save_students
import residetrack.shared.feature_manage_students.impl.generated.resources.error_invalid_date_format
import residetrack.shared.feature_manage_students.impl.generated.resources.error_invalid_date_range
import residetrack.shared.feature_manage_students.impl.generated.resources.error_room_not_found
import residetrack.shared.feature_manage_students.impl.generated.resources.error_stream_number_invalid
import residetrack.shared.feature_manage_students.impl.generated.resources.error_stream_number_required
import residetrack.shared.feature_manage_students.impl.generated.resources.error_student_already_exists
import residetrack.shared.feature_manage_students.impl.generated.resources.students_saved_successfully

internal interface StringProvider {
    suspend fun getStreamNumberRequired(): String
    suspend fun getStreamNumberInvalid(): String
    suspend fun getCheckInDateRequired(): String
    suspend fun getCheckOutDateRequired(): String
    suspend fun getInvalidDateRange(): String
    suspend fun getDuplicateStreamNumbers(): String
    suspend fun getInvalidDateFormat(): String
    suspend fun getStudentAlreadyExists(): String
    suspend fun getFailedToLoadStudents(): String
    suspend fun getFailedToSaveStudents(): String
    suspend fun getRoomNotFound(): String
    suspend fun getDraftRoomNotFound(): String
    suspend fun getStudentsSavedSuccessfully(): String
}

internal class StringProviderImpl : StringProvider {
    override suspend fun getStreamNumberRequired(): String =
        getString(Res.string.error_stream_number_required)

    override suspend fun getStreamNumberInvalid(): String =
        getString(Res.string.error_stream_number_invalid)

    override suspend fun getCheckInDateRequired(): String =
        getString(Res.string.error_check_in_date_required)

    override suspend fun getCheckOutDateRequired(): String =
        getString(Res.string.error_check_out_date_required)

    override suspend fun getInvalidDateRange(): String =
        getString(Res.string.error_invalid_date_range)

    override suspend fun getDuplicateStreamNumbers(): String =
        getString(Res.string.error_duplicate_stream_numbers)

    override suspend fun getInvalidDateFormat(): String =
        getString(Res.string.error_invalid_date_format)

    override suspend fun getStudentAlreadyExists(): String =
        getString(Res.string.error_student_already_exists)

    override suspend fun getFailedToLoadStudents(): String =
        getString(Res.string.error_failed_to_load_students)

    override suspend fun getFailedToSaveStudents(): String =
        getString(Res.string.error_failed_to_save_students)

    override suspend fun getRoomNotFound(): String =
        getString(Res.string.error_room_not_found)

    override suspend fun getDraftRoomNotFound(): String =
        getString(Res.string.error_draft_room_not_found)

    override suspend fun getStudentsSavedSuccessfully(): String =
        getString(Res.string.students_saved_successfully)
}
