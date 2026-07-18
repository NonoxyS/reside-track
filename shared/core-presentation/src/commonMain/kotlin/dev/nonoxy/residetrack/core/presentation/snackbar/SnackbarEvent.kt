package dev.nonoxy.residetrack.core.presentation.snackbar

import dev.icerock.moko.resources.StringResource

data class SnackbarEvent(
    val message: StringResource,
    val type: SnackbarEventType,
)
