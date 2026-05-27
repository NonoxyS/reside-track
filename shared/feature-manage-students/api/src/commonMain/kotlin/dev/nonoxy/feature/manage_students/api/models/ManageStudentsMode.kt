package dev.nonoxy.feature.manage_students.api.models

sealed interface ManageStudentsMode {
    data class ExistingRoom(val roomId: String) : ManageStudentsMode
    data object DraftRoom : ManageStudentsMode
}
