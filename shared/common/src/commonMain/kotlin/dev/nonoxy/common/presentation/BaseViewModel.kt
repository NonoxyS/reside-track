package dev.nonoxy.common.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<State : Any, Event, Action>(initialState: State) : ViewModel() {

    private val _viewState: MutableStateFlow<State> = MutableStateFlow(value = initialState)
    fun viewState(): StateFlow<State> = _viewState.asStateFlow()

    protected var viewState: State
        get() = _viewState.value
        set(value) {
            _viewState.update { value }
        }

    private val _viewAction: Channel<Action> = Channel(Channel.BUFFERED)
    fun viewAction(): Flow<Action> = _viewAction.receiveAsFlow()

    protected var viewAction: Action
        @Deprecated(
            message = "Getting action from a Channel is not supported",
            level = DeprecationLevel.ERROR
        )
        get() = throw UnsupportedOperationException("Cannot get action from Channel")
        set(value) {
            _viewAction.trySend(value)
        }

    abstract fun obtainEvent(event: Event)
}
