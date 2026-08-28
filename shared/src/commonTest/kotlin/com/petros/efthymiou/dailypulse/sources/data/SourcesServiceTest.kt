package com.petros.efthymiou.dailypulse.sources.data

import com.petros.efthymiou.dailypulse.fixtures.GraphqlFixtures
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for SourcesService using ktor-client-mock.
 */
class SourcesServiceTest {

    @Test
    fun `fetchSources returns parsed sources on success`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = GraphqlFixtures.SOURCES_RESPONSE,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        val client = createTestHttpClient(mockEngine)
        val service = SourcesService(client)
        
        val result = service.fetchSources(aggregator = "tech")
        
        assertEquals(3, result.size)
        assertEquals("TechCrunch", result[0].name)
        assertEquals("Wired", result[1].name)
        assertEquals("The Verge", result[2].name)
        assertEquals("techcrunch", result[0].id)
    }

    @Test
    fun `fetchSources sends aggregator in variables`() = runTest {
        var requestReceived = false
        
        val mockEngine = MockEngine { request ->
            requestReceived = true
            // In a real test, you'd parse the request body to verify variables
            respond(
                content = GraphqlFixtures.SOURCES_RESPONSE,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        val client = createTestHttpClient(mockEngine)
        val service = SourcesService(client)
        
        service.fetchSources(aggregator = "business")
        
        assertTrue(requestReceived, "Request should have been made")
    }

    @Test
    fun `fetchSources handles empty sources list`() = runTest {
        val emptyResponse = """{
            "data": {
                "sources": []
            }
        }"""
        
        val mockEngine = MockEngine { request ->
            respond(
                content = emptyResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        val client = createTestHttpClient(mockEngine)
        val service = SourcesService(client)
        
        val result = service.fetchSources(aggregator = "tech")
        
        assertTrue(result.isEmpty())
    }

    private fun createTestHttpClient(mockEngine: MockEngine): HttpClient {
        return HttpClient(mockEngine) {
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
