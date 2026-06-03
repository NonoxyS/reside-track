package dev.nonoxy.residetrack.core.initializer

/**
 * A single startup task executed by [AppInitializer] before the app's first screen is shown.
 *
 * Initializers are grouped by [priority]: all initializers sharing a priority run concurrently,
 * while different priorities run sequentially in ascending order (a barrier between groups). Use the
 * same priority for independent tasks and a higher priority for tasks that must run after others.
 */
interface Initializer {

    /** Lower runs first. Same value within a group runs in parallel. */
    val priority: Int

    /**
     * When `true` (default), a thrown exception aborts startup. When `false`, the failure is caught
     * and logged so the remaining startup continues.
     */
    val isCritical: Boolean
        get() = true

    suspend fun initialize()
}
