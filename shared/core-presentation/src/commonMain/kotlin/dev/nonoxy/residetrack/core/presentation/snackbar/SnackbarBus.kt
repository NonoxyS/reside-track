package dev.nonoxy.residetrack.core.presentation.snackbar

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class SnackbarBus {
    private val channel = Channel<SnackbarEvent>(Channel.BUFFERED)
    val events: Flow<SnackbarEvent> = channel.receiveAsFlow()

    fun send(event: SnackbarEvent) {
        channel.trySend(event)
    }
}
