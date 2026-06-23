package dev.nonoxy.residetrack.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
