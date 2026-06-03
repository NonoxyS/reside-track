package dev.nonoxy.residetrack.core.initializer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

class AppInitializerTest {

    @Test
    fun run_with_no_initializers_completes() = runTest {
        AppInitializer(emptyList()).run()
    }

    @Test
    fun run_invokes_initialize() = runTest {
        val initializer = FakeInitializer()

        AppInitializer(listOf(initializer)).run()

        assertTrue(initializer.initialized)
    }

    @Test
    fun critical_initializer_failure_propagates() = runTest {
        val failing = FakeInitializer(
            isCritical = true,
            onInitialize = { error("boom") },
        )

        assertFailsWith<IllegalStateException> {
            AppInitializer(listOf(failing)).run()
        }
    }

    @Test
    fun non_critical_failure_is_swallowed_and_siblings_still_run() = runTest {
        val failing = FakeInitializer(
            priority = 0,
            isCritical = false,
            onInitialize = { error("boom") },
        )
        val sibling = FakeInitializer(priority = 0)

        // run() must not throw despite the non-critical failure.
        AppInitializer(listOf(failing, sibling)).run()

        assertTrue(sibling.initialized)
    }

    @Test
    fun groups_run_in_ascending_priority_order() = runTest {
        val order = mutableListOf<Int>()
        val high = FakeInitializer(priority = 10, onInitialize = { order.add(10) })
        val low = FakeInitializer(priority = 1, onInitialize = { order.add(1) })

        // Listed high-first to prove ordering comes from priority, not list order.
        AppInitializer(listOf(high, low)).run()

        assertEquals(listOf(1, 10), order)
    }

    @Test
    fun lower_priority_group_fully_completes_before_next_group_starts() = runTest {
        val order = mutableListOf<String>()
        val first = FakeInitializer(
            priority = 0,
            onInitialize = {
                delay(100)
                order.add("first")
            },
        )
        val second = FakeInitializer(
            priority = 1,
            onInitialize = { order.add("second") },
        )

        // Barrier: even though "first" suspends, it must finish before "second" starts.
        AppInitializer(listOf(first, second)).run()

        assertEquals(listOf("first", "second"), order)
    }

    @Test
    fun initializers_in_same_priority_run_concurrently() = runTest {
        val signal = CompletableDeferred<Unit>()
        val waiter = FakeInitializer(
            priority = 0,
            // Deadlocks if the group runs sequentially: releaser never gets a turn.
            onInitialize = { signal.await() },
        )
        val releaser = FakeInitializer(
            priority = 0,
            onInitialize = { signal.complete(Unit) },
        )

        AppInitializer(listOf(waiter, releaser)).run()

        assertTrue(waiter.initialized)
        assertTrue(releaser.initialized)
    }

    private class FakeInitializer(
        override val priority: Int = 0,
        override val isCritical: Boolean = true,
        private val onInitialize: suspend () -> Unit = {},
    ) : Initializer {

        var initialized = false
            private set

        override suspend fun initialize() {
            initialized = true
            onInitialize()
        }
    }
}
