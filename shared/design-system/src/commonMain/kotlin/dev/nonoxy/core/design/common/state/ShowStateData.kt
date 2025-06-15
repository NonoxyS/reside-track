package dev.nonoxy.core.design.common.state

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

private sealed interface InnerState {
    data object Loading : InnerState
    data object Error : InnerState
    data object Data : InnerState
}

@Composable
fun <T> ShowStateData(
    state: T,
    isLoading: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit = {},
    loadingState: @Composable BoxScope.() -> Unit = { LoadingState() },
    errorState: @Composable BoxScope.() -> Unit = { ErrorLoadingState(onRetryClick = onRetryClick) },
    dataState: @Composable BoxScope.(state: T) -> Unit
) {
    val currentInnerState: InnerState by remember(isLoading, isError) {
        mutableStateOf(
            when {
                isError -> InnerState.Error
                isLoading -> InnerState.Loading
                else -> InnerState.Data
            }
        )
    }

    Crossfade(
        targetState = currentInnerState
    ) { innerState ->
        Box(modifier = modifier) {
            when (innerState) {
                InnerState.Data -> dataState(state)
                InnerState.Error -> errorState()
                InnerState.Loading -> loadingState()
            }
        }
    }
}
