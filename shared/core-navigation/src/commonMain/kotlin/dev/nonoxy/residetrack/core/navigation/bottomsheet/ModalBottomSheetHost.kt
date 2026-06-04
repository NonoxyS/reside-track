package dev.nonoxy.residetrack.core.navigation.bottomsheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation.compose.LocalOwnersProvider
import kotlinx.coroutines.launch

@Suppress("ModifierMissing")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ModalBottomSheetHost(
    modalBottomSheetNavigator: ModalBottomSheetNavigator
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    val bottomSheetBackStack by modalBottomSheetNavigator.backStack.collectAsState()
    val transitionInProgress by modalBottomSheetNavigator.transitionInProgress.collectAsState()

    val currentEntry = bottomSheetBackStack.lastOrNull()
    val configuration =
        (currentEntry?.destination as? ModalBottomSheetNavigator.Destination)?.configuration

    val scope = rememberCoroutineScope()
    val dismissDispatcher = remember { SheetDismissDispatcher() }
    val baseConfirmValueChange = configuration.getConfirmValueChange()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = configuration.getSkipPartiallyExpanded(),
        confirmValueChange = { sheetValue ->
            if (sheetValue == SheetValue.Hidden && dismissDispatcher.hasEnabledCallback) {
                dismissDispatcher.dispatch()
                false
            } else {
                baseConfirmValueChange(sheetValue)
            }
        }
    )

    LaunchedEffect(bottomSheetBackStack.size) {
        if (bottomSheetBackStack.isNotEmpty()) {
            if (!sheetState.isVisible) {
                sheetState.show()
            }
        }
    }

    if (bottomSheetBackStack.isNotEmpty()) {

        if (configuration.getProperties().shouldDismissOnBackPress) {
            BackHandler {
                if (dismissDispatcher.hasEnabledCallback) {
                    dismissDispatcher.dispatch()
                } else {
                    scope.launch {
                        sheetState.hide()
                    }
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = {
                if (dismissDispatcher.hasEnabledCallback) {
                    dismissDispatcher.dispatch()
                } else {
                    currentEntry?.let { entry ->
                        modalBottomSheetNavigator.dismiss(entry)
                    }
                }
            },
            sheetState = sheetState,
            modifier = configuration.getModifier(),
            sheetMaxWidth = configuration.getSheetMaxWidth(),
            shape = configuration.getShape(),
            properties = configuration.getProperties(),
            containerColor = configuration.getContainerColor(),
            contentColor = configuration.getContentColor(),
            tonalElevation = configuration.getTonalElevation(),
            scrimColor = configuration.getScrimColor(),
            dragHandle = configuration.getDragHandle(),
            contentWindowInsets = configuration.getContentWindowInsets(),
        ) {
            AnimatedContent(
                targetState = currentEntry,
                transitionSpec = configuration.getContentTransition(),
                label = "BottomSheetContentAnimation"
            ) { entry ->
                entry?.let { backStackEntry ->
                    DisposableEffect(backStackEntry) {
                        onDispose {
                            modalBottomSheetNavigator.onTransitionComplete(backStackEntry)
                        }
                    }

                    backStackEntry.LocalOwnersProvider(saveableStateHolder) {
                        val destination =
                            backStackEntry.destination as ModalBottomSheetNavigator.Destination
                        CompositionLocalProvider(
                            LocalSheetDismissDispatcher provides dismissDispatcher
                        ) {
                            destination.content(backStackEntry)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(transitionInProgress, currentEntry) {
        transitionInProgress.forEach { entry ->
            if (entry != currentEntry && !bottomSheetBackStack.contains(entry)) {
                modalBottomSheetNavigator.onTransitionComplete(entry)
            }
        }
    }
}
