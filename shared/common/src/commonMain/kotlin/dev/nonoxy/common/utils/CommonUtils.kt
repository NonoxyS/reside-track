package dev.nonoxy.common.utils

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf

fun <T> T.wrapSuccess(): Result<T> = Result.success(this)

fun <T> Throwable.wrapFailure(): Result<T> = Result.failure(this)

fun <T> ImmutableList<T>?.orEmptyPersist(): ImmutableList<T> = this ?: persistentListOf()

fun <T> ImmutableSet<T>?.orEmptyPersist(): ImmutableSet<T> = this ?: persistentSetOf()

fun <K, V> ImmutableMap<K, V>?.orEmptyPersist(): ImmutableMap<K, V> = this ?: persistentMapOf()
