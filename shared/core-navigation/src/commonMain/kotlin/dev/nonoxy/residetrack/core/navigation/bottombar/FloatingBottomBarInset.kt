package dev.nonoxy.residetrack.core.navigation.bottombar

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Bottom space occupied by the floating bottom bar (its measured height plus
 * navigation-bar inset and outer margins). Scrollable content under the bar
 * adds this to its bottom contentPadding so the last items can clear the bar.
 */
val LocalFloatingBottomBarInset = compositionLocalOf { 0.dp }
