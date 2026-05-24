package dev.nonoxy.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
val mainImmediateDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
val unconfinedDispatcher: CoroutineDispatcher = Dispatchers.Unconfined
