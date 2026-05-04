package com.petros.efthymiou.dailypulse.android

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Minimal smoke test shared by both **mpp** and **native** flavors: verifies that
 * [MainActivity] can be created without crashing (each flavor compiles its own
 * [MainActivity] at the same FQCN).
 */
@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    @Test
    fun mainActivity_launchesWithoutCrashing() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity)
            }
        }
    }
}
