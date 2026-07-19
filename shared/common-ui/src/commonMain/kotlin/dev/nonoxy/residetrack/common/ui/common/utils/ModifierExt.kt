package dev.nonoxy.residetrack.common.ui.common.utils

import androidx.compose.ui.Modifier

fun Modifier.thenIf(
    condition: Boolean,
    other: Modifier.() -> Modifier,
): Modifier = if (condition) this.other() else this
