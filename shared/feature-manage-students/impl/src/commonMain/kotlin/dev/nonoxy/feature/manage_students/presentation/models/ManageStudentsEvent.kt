package dev.nonoxy.feature.manage_students.presentation.models

internal sealed class ManageStudentsEvent {

    data object LoadStudents : ManageStudentsEvent()

    data object OnAddStudent : ManageStudentsEvent()

    data class OnRemoveStudent(val studentId: String) : ManageStudentsEvent()

    data class OnStreamNumberChange(val studentId: String, val value: String) : ManageStudentsEvent()

    data class OnCheckInDateChange(val studentId: String, val value: String) : ManageStudentsEvent()

    data class OnCheckOutDateChange(val studentId: String, val value: String) : ManageStudentsEvent()

    data class OnCheckInDateMillisChange(val studentId: String, val millis: Long) : ManageStudentsEvent()

    data class OnCheckOutDateMillisChange(val studentId: String, val millis: Long) : ManageStudentsEvent()

    data object OnSaveAndClose : ManageStudentsEvent()

    data object OnClose : ManageStudentsEvent()
} 