package com.petros.efthymiou.dailypulse.android.di

import com.petros.efthymiou.dailypulse.network.TestBffConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

/**
 * Test Koin module for instrumented tests.
 * 
 * This module overrides the network configuration to point to MockWebServer.
 * Usage in tests:
 * 
 * ```kotlin
 * @Before
 * fun setUp() {
 *     mockWebServer = MockWebServer().apply { start(0) }
 *     loadKoinModules(createTestNetworkModule(mockWebServer.url("/").toString()))
 * }
 * ```
 */
object TestKoinModules {

    /**
     * Creates a test network module that configures the HttpClient to use a custom URL.
     * This allows tests to point to MockWebServer running on a random port.
     * 
     * @param mockServerUrl The base URL of the MockWebServer (e.g., "http://127.0.0.1:12345")
     */
    fun createTestNetworkModule(mockServerUrl: String) = module {
        // Set the runtime override for TestBffConfig
        single {
            TestBffConfig.setOverride(mockServerUrl)
            // Return the same HttpClient configuration as production
            HttpClient {
                expectSuccess = false
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                    })
                }
            }
        }
    }

    /**
     * Alternative approach: directly override the HttpClient without TestBffConfig.
     * This is useful if you want more control over the client configuration in tests.
     */
    fun createTestHttpClientModule(httpClient: HttpClient) = module {
        single { httpClient }
    }
}
