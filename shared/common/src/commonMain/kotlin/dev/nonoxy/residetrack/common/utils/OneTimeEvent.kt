package dev.nonoxy.residetrack.common.utils

import kotlinx.coroutines.channels.Channel

@Suppress("FunctionName")
fun <T> OneTimeEvent(): Channel<T> = Channel(Channel.BUFFERED)
