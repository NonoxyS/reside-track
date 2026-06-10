package dev.nonoxy.residetrack.feature.room_editor.presentation.models

/** Which date field of which student card currently has its picker open. */
data class UiOpenDatePicker(val studentId: String, val field: UiDateField)

enum class UiDateField { CHECK_IN, CHECK_OUT }
