package dev.nonoxy.residetrack.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "dev.nonoxy.residetrack",
    ) {
        startActivityAndWait()
        // Extend with key user journeys for a richer profile:
        // - scroll room list
        // - open room editor
        // - navigate to upcoming tab
    }
}
