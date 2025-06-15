package dev.nonoxy.common.utils

import kotlinx.coroutines.CancellationException

inline fun <T, R> T.coRunCatching(
    tryBlock: T.() -> R,
    catchBlock: (Throwable) -> R
): R {
    return try {
        tryBlock()
    } catch (throwable: Throwable) {
        when (throwable) {
            is CancellationException -> throw throwable
            else -> catchBlock(throwable)
        }
    }
}

inline fun <T, R> T.coRunCatching(
    tryBlock: T.() -> R,
    catchBlock: (Throwable) -> R,
    finallyBlock: T.() -> Unit
): R {
    return try {
        tryBlock()
    } catch (throwable: Throwable) {
        when (throwable) {
            is CancellationException -> throw throwable
            else -> catchBlock(throwable)
        }
    } finally {
        finallyBlock()
    }
}
