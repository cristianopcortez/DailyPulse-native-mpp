package com.petros.efthymiou.dailypulse.android

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Custom test runner that uses TestDailyPulseApp instead of DailyPulseApp.
 * 
 * This allows tests to inject MockWebServer configuration before Koin initializes.
 * 
 * Configure in build.gradle.kts:
 * ```kotlin
 * android {
 *     defaultConfig {
 *         testInstrumentationRunner = "com.petros.efthymiou.dailypulse.android.DailyPulseTestRunner"
 *     }
 * }
 * ```
 */
class DailyPulseTestRunner : AndroidJUnitRunner() {
    
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, TestDailyPulseApp::class.java.name, context)
    }
}
