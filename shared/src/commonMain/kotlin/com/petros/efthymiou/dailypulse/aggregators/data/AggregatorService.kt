package com.petros.efthymiou.dailypulse.aggregators.data

import com.petros.efthymiou.dailypulse.network.BffConfig
import com.petros.efthymiou.dailypulse.network.TestBffConfig
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

class AggregatorService(private val httpClient: HttpClient) {

    suspend fun fetchAggregators(): List<AggregatorRaw> {
        val response: GraphqlResponse<AggregatorsGraphqlData> = httpClient.post(TestBffConfig.getGraphqlUrl()) {
            contentType(ContentType.Application.Json)
            setBody(GraphqlRequest(query = GraphqlQueries.AGGREGATORS))
        }.body()

        return response.requireData().aggregators
    }
}
