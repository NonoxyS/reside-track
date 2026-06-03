package dev.nonoxy.residetrack.core.initializer

import dev.nonoxy.residetrack.common.utils.coRunCatching
import io.github.aakira.napier.Napier
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

/**
 * Runs all registered [Initializer]s grouped by [Initializer.priority].
 *
 * Within a priority group the initializers run concurrently; groups run sequentially in ascending
 * priority order, so a group only starts once the previous one has fully completed.
 *
 * Critical initializers (default) propagate failures and abort startup. Non-critical ones have their
 * failure logged and swallowed so it does not affect siblings or the rest of startup.
 */
class AppInitializer(
    private val initializers: List<Initializer>,
) {

    suspend fun run() {
        initializers
            .groupBy { initializer -> initializer.priority }
            .toList()
            .sortedBy { (priority, _) -> priority }
            .forEach { (_, group) ->
                supervisorScope {
                    group
                        .map { initializer -> async { initializer.runSafely() } }
                        .awaitAll()
                }
            }
    }

    private suspend fun Initializer.runSafely() {
        if (isCritical) {
            initialize()
            return
        }

        coRunCatching(
            tryBlock = { initialize() },
            catchBlock = { throwable ->
                Napier.e(
                    throwable = throwable,
                    message = "Initializer ${this::class.simpleName} failed",
                )
            }
        )
    }
}
