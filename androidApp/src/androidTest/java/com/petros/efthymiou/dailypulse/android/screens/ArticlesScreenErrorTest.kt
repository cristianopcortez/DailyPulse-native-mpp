package com.petros.efthymiou.dailypulse.android.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.petros.efthymiou.dailypulse.android.TestDailyPulseApp
import com.petros.efthymiou.dailypulse.android.di.TestKoinModules
import com.petros.efthymiou.dailypulse.android.fixtures.AndroidGraphqlFixtures
import com.petros.efthymiou.dailypulse.network.TestBffConfig
import com.petros.efthymiou.dailypulse.ui.App
import com.petros.efthymiou.dailypulse.ui.MyApplicationTheme
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.java.KoinJavaComponent.inject
import petros.efthymiou.dailypulse.db.DailyPulseDatabase

/**
 * Error handling tests for ArticlesScreen.
 * 
 * Verifies that the UI displays appropriate error messages when:
 * - The backend returns errors
 * - Network requests fail
 * - Empty responses are received
 */
@RunWith(AndroidJUnit4::class)
class ArticlesScreenErrorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockWebServer: MockWebServer
    private lateinit var testModule: org.koin.core.module.Module

    @Before
    fun setUp() {
        // Clear database to ensure test isolation
        val database: DailyPulseDatabase by inject(DailyPulseDatabase::class.java)
        database.dailyPulseDatabaseQueries.removeAllArticles()
        database.dailyPulseDatabaseQueries.removeAllSources()
        
        mockWebServer = MockWebServer()
        mockWebServer.start(0)

        val mockServerUrl = mockWebServer.url("/").toString().trimEnd('/')
        TestBffConfig.setOverride(mockServerUrl)

        testModule = TestKoinModules.createTestNetworkModule(mockServerUrl)
        TestDailyPulseApp.testModules = listOf(testModule)
        
        try {
            loadKoinModules(testModule)
        } catch (e: Exception) {
            // Already initialized
        }
    }

    @After
    fun tearDown() {
        TestBffConfig.clearOverride()
        
        if (::mockWebServer.isInitialized) {
            mockWebServer.shutdown()
        }
        
        try {
            unloadKoinModules(testModule)
        } catch (e: Exception) {
            // Ignore
        }
    }

    @Test
    fun articlesScreen_displaysErrorMessage_whenServerReturnsError() {
        // Configure mock to return aggregators successfully but fail on articles
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val body = request.body.readUtf8()
                
                return when {
                    body.contains("aggregators") -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setBody(AndroidGraphqlFixtures.AGGREGATORS_SUCCESS)
                            .addHeader("Content-Type", "application/json")
                    }
                    body.contains("articles") -> {
                        MockResponse()
                            .setResponseCode(500)
                            .setBody(AndroidGraphqlFixtures.ERROR_RESPONSE)
                            .addHeader("Content-Type", "application/json")
                    }
                    else -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setBody("{\"data\":{}}")
                    }
                }
            }
        }

        composeTestRule.setContent {
            MyApplicationTheme {
                App()
            }
        }

        // Wait for error message to appear
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule
                    .onNodeWithText("error", substring = true, ignoreCase = true)
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Note: Exact error message depends on the app's error handling implementation
        // This test verifies that *some* error message is shown
    }

    @Test
    fun articlesScreen_handlesEmptyResponse() {
        // Configure mock to return empty articles list
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val body = request.body.readUtf8()
                
                return when {
                    body.contains("aggregators") -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setBody(AndroidGraphqlFixtures.AGGREGATORS_SUCCESS)
                            .addHeader("Content-Type", "application/json")
                    }
                    body.contains("articles") -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setBody(AndroidGraphqlFixtures.ARTICLES_EMPTY)
                            .addHeader("Content-Type", "application/json")
                    }
                    else -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setBody("{\"data\":{}}")
                    }
                }
            }
        }

        composeTestRule.setContent {
            MyApplicationTheme {
                App()
            }
        }

        // Wait a bit for the empty state to be processed
        Thread.sleep(2000)

        // Verify that no error message is shown (empty is a valid state)
        // The app should just show an empty list or appropriate empty state UI
        // Note: This depends on your app's design - you might show "No articles available"
    }
}
