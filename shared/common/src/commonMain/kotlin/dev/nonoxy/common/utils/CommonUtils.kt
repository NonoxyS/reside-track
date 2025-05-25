package dev.nonoxy.common.utils

fun <T> T.wrapSuccess(): Result<T> = Result.success(this)

fun <T> Throwable.wrapFailure(): Result<T> = Result.failure(this)
