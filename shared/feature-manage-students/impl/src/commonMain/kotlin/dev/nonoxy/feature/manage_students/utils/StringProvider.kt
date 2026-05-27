package dev.nonoxy.feature.manage_students.utils

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

// TODO Task 16: remove together with OldManageStudentsViewModel.
internal class StringProviderImpl : StringProvider {
    override suspend fun getStreamNumberRequired(): String = "Stream number is required"
    override suspend fun getStreamNumberInvalid(): String = "Stream number must be a number"
    override suspend fun getCheckInDateRequired(): String = "Check-in date is required"
    override suspend fun getCheckOutDateRequired(): String = "Check-out date is required"
    override suspend fun getInvalidDateRange(): String = "Check-out date must be after check-in date"
    override suspend fun getDuplicateStreamNumbers(): String = "Duplicate stream numbers found"
    override suspend fun getInvalidDateFormat(): String = "Invalid date format"
    override suspend fun getStudentAlreadyExists(): String = "Student with this stream number already exists"
    override suspend fun getFailedToLoadStudents(): String = "Failed to load students"
    override suspend fun getFailedToSaveStudents(): String = "Failed to save students"
    override suspend fun getRoomNotFound(): String = "Room not found"
    override suspend fun getDraftRoomNotFound(): String = "Draft room not found"
    override suspend fun getStudentsSavedSuccessfully(): String = "Students saved successfully"
}
