package dev.nonoxy.core.navigation.bottom_sheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.navigation.compose.LocalOwnersProvider
import dev.nonoxy.core.navigation.BackHandler
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = configuration.getSkipPartiallyExpanded(),
        confirmValueChange = configuration.getConfirmValueChange()
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
                scope.launch {
                    sheetState.hide()
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = {
                currentEntry?.let { entry ->
                    modalBottomSheetNavigator.dismiss(entry)
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
                        destination.content(backStackEntry)
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
