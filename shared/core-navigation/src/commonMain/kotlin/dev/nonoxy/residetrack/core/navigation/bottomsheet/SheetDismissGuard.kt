package dev.nonoxy.residetrack.core.navigation.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * A single registered dismiss-veto from bottom-sheet content.
 *
 * @property enabled while `true`, the host must not dismiss the sheet on swipe/scrim/back and
 *   should instead invoke [onBlockedAttempt].
 * @property onBlockedAttempt fired when a dismiss was blocked — content turns this into an Intent.
 */
class SheetDismissCallback internal constructor(
    internal var enabled: Boolean,
    internal val onBlockedAttempt: () -> Unit,
)

/**
 * Registry that lets bottom-sheet content veto dismiss attempts (swipe / scrim / back press).
 *
 * Mirrors the `OnBackPressedDispatcher` pattern: content registers a callback via
 * [SheetDismissGuard]; the host consults [hasEnabledCallback] before dismissing and calls
 * [dispatch] when a dismiss is blocked. The host owns the instance; content reaches it through
 * [LocalSheetDismissDispatcher].
 */
class SheetDismissDispatcher internal constructor() {
    private val callbacks = mutableStateListOf<SheetDismissCallback>()

    /** `true` when at least one registered callback currently vetoes dismissal. */
    val hasEnabledCallback: Boolean
        get() = callbacks.any { it.enabled }

    internal fun register(callback: SheetDismissCallback) {
        callbacks.add(callback)
    }

    internal fun unregister(callback: SheetDismissCallback) {
        callbacks.remove(callback)
    }

    /** Notify the top-most enabled callback that a dismiss attempt was blocked. */
    fun dispatch() {
        callbacks.lastOrNull { it.enabled }?.onBlockedAttempt?.invoke()
    }
}

val LocalSheetDismissDispatcher = staticCompositionLocalOf<SheetDismissDispatcher> {
    error("LocalSheetDismissDispatcher not provided. Sheet content must be hosted by ModalBottomSheetHost.")
}

/**
 * Registers a dismiss veto for the enclosing bottom sheet.
 *
 * While [enabled] is `true`, swipe-down / scrim-tap / back-press will not close the sheet; instead
 * [onBlockedAttempt] runs (typically sending an Intent to the store, which then decides whether to
 * show a confirm dialog or close). Registration follows the sheet content lifecycle.
 */
@Composable
fun SheetDismissGuard(
    enabled: Boolean,
    onBlockedAttempt: () -> Unit,
) {
    val dispatcher = LocalSheetDismissDispatcher.current
    val currentOnBlocked = rememberUpdatedState(onBlockedAttempt)
    val callback = remember { SheetDismissCallback(enabled) { currentOnBlocked.value() } }

    SideEffect { callback.enabled = enabled }

    DisposableEffect(dispatcher, callback) {
        dispatcher.register(callback)
        onDispose { dispatcher.unregister(callback) }
    }
}
