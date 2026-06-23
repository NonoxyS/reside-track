package dev.nonoxy.residetrack.feature.room_editor.presentation.models

data class UiEditableStudent(
    val id: String,
    val studentId: Long?,
    val streamNumber: String,
    val checkInDate: String,
    val checkOutDate: String,
    val checkInDateMillis: Long? = null,
    val checkOutDateMillis: Long? = null,
    val isNew: Boolean = false,
)
