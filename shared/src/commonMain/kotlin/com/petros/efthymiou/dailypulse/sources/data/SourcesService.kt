package com.petros.efthymiou.dailypulse.sources.data

import com.petros.efthymiou.dailypulse.network.BffConfig
import com.petros.efthymiou.dailypulse.network.GraphqlQueries
import com.petros.efthymiou.dailypulse.network.GraphqlRequest
import com.petros.efthymiou.dailypulse.network.GraphqlResponse
import com.petros.efthymiou.dailypulse.network.requireData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SourcesService(private val httpClient: HttpClient) {

    suspend fun fetchSources(aggregator: String): List<SourceRaw> {
        val variables = buildJsonObject {
            put("aggregator", aggregator)
        }

        val request = GraphqlRequest(
            query = GraphqlQueries.SOURCES,
            variables = variables
        )

        val response: GraphqlResponse<SourcesGraphqlData> = httpClient.post(BffConfig.graphqlUrl) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

        return response.requireData().sources
    }
}
