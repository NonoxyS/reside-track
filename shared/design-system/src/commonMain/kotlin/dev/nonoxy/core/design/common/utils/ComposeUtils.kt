package dev.nonoxy.core.design.common.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

@Composable
fun <T> Flow<T>.CollectFlow(
    collector: suspend (T) -> Unit
) {
    LaunchedEffect(Unit) {
        collect(collector = collector)
    }
}
