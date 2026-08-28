package com.petros.efthymiou.dailypulse.android

import android.app.Application
import com.petros.efthymiou.dailypulse.android.di.databaseModule
import com.petros.efthymiou.dailypulse.android.di.viewModelsModule
import com.petros.efthymiou.dailypulse.di.sharedKoinModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/**
 * Test Application for instrumented tests.
 * 
 * Allows injecting test modules (e.g., MockWebServer configuration) before Koin starts.
 * 
 * Usage in instrumented tests:
 * Configure this application in your test's AndroidManifest.xml or via a custom test runner.
 */
class TestDailyPulseApp : Application() {

    companion object {
        /**
         * Additional modules to load during Koin initialization.
         * Set this before the app starts (e.g., in a custom AndroidJUnitRunner).
         */
        var testModules: List<Module> = emptyList()
    }

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        val modules = sharedKoinModules + viewModelsModule + databaseModule + testModules

        startKoin {
            androidContext(this@TestDailyPulseApp)
            modules(modules)
        }
    }
}
