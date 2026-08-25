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

class SourcesService(private val httpClient: HttpClient) {

    suspend fun fetchSources(): List<SourceRaw> {
        val response: GraphqlResponse<SourcesGraphqlData> = httpClient.post(BffConfig.graphqlUrl) {
            contentType(ContentType.Application.Json)
            setBody(GraphqlRequest(query = GraphqlQueries.SOURCES))
        }.body()

        return response.requireData().sources
    }
}
