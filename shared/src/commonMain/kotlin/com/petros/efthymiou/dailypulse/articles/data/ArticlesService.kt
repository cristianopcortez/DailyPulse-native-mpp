package com.petros.efthymiou.dailypulse.articles.data

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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ArticlesService(private val httpClient: HttpClient) {

    suspend fun fetchArticles(aggregator: String, source: String? = null): List<ArticleRaw> {
        val variables = buildJsonObject {
            put("aggregator", aggregator)
            source?.let { put("source", it) }
        }

        val request = GraphqlRequest(
            query = GraphqlQueries.ARTICLES,
            variables = variables,
        )

        val response: GraphqlResponse<ArticlesGraphqlData> = httpClient.post(BffConfig.graphqlUrl) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

        return response.requireData().articles
    }
}
