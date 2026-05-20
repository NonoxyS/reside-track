package dev.nonoxy.feature.manage_students.models

sealed interface ManageStudentsMode {
    data class ExistingRoom(val roomId: String) : ManageStudentsMode
    data object DraftRoom : ManageStudentsMode
}
