package com.petros.efthymiou.dailypulse.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Ktor client that answers GraphQL the same way Android MockWebServer does,
 * so iOS XCUITests stay deterministic on the Codemagic simulator.
 */
fun createUiTestHttpClient(scenario: String): HttpClient {
    val mockEngine = MockEngine { request ->
        val body = requestBodyAsText(request.body)
        val payload = when {
            scenario == UiTestScenario.ERROR && body.contains("articles") ->
                UiTestGraphqlFixtures.ERROR_RESPONSE
            body.contains("aggregators") -> UiTestGraphqlFixtures.AGGREGATORS_SUCCESS
            body.contains("sources") -> UiTestGraphqlFixtures.SOURCES_SUCCESS
            body.contains("articles") -> UiTestGraphqlFixtures.ARTICLES_SUCCESS
            scenario == UiTestScenario.ERROR -> UiTestGraphqlFixtures.ERROR_RESPONSE
            else -> UiTestGraphqlFixtures.ARTICLES_SUCCESS
        }
        respond(
            content = payload,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    }

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

private fun requestBodyAsText(content: OutgoingContent): String {
    return when (content) {
        is TextContent -> content.text
        is OutgoingContent.ByteArrayContent -> content.bytes().decodeToString()
        else -> content.toString()
    }
}
