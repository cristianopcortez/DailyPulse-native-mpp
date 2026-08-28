package com.petros.efthymiou.dailypulse.network

import kotlin.concurrent.Volatile

/**
 * Test-friendly BFF configuration that allows runtime override of the base URL.
 * 
 * Usage:
 * - Production: uses the compile-time baked value from BffBuildConfig
 * - Tests: call `TestBffConfig.setOverride("http://127.0.0.1:12345")` before launching the app
 * 
 * This enables MockWebServer integration in androidTest without needing separate build flavors.
 */
object TestBffConfig {
    
    /**
     * Runtime override for tests. Set this before the app initializes Koin.
     * Null means use the default compile-time value.
     */
    @Volatile
    private var overrideUrl: String? = null
    
    /**
     * Get the effective base URL: runtime override if set, otherwise compile-time default.
     */
    fun getBaseUrl(): String = overrideUrl ?: bffBaseUrl
    
    /**
     * Get the effective GraphQL URL.
     */
    fun getGraphqlUrl(): String = "${getBaseUrl().trimEnd('/')}/graphql"
    
    /**
     * Set a runtime override for the BFF URL. Used by tests with MockWebServer.
     * Pass null to clear the override and revert to the default.
     */
    fun setOverride(url: String?) {
        overrideUrl = url
    }
    
    /**
     * Clear the runtime override. Useful in test teardown.
     */
    fun clearOverride() {
        overrideUrl = null
    }
}
