package com.petros.efthymiou.dailypulse.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class GraphqlException(message: String) : Exception(message)

@Serializable
data class GraphqlRequest(
    val query: String,
    val variables: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class GraphqlResponse<T>(
    val data: T? = null,
    val errors: List<GraphqlError>? = null,
)

@Serializable
data class GraphqlError(
    val message: String = "Unable to load data",
)

object GraphqlQueries {
    const val ARTICLES =
        "query Articles(\$source: String) { articles(source: \$source) { title desc date imageUrl } }"

    const val SOURCES =
        "query Sources { sources { id name desc origin } }"

    const val AGGREGATORS =
        "query Aggregators { aggregators { id name } }"
}

fun <T> GraphqlResponse<T>.requireData(): T {
    val graphQlMessage = errors?.firstOrNull()?.message?.takeIf { it.isNotBlank() }
    if (graphQlMessage != null) {
        throw GraphqlException(graphQlMessage)
    }
    return data ?: throw GraphqlException("Unable to load data")
}
