package com.petros.efthymiou.dailypulse.network

/**
 * Single switch for the BFF origin. The value is baked in at compile time by the
 * `generateBffConfig` Gradle task; each platform keeps its own local default.
 */
expect val bffBaseUrl: String

object BffConfig {
    val baseUrl: String get() = bffBaseUrl
    val graphqlUrl: String get() = "${bffBaseUrl.trimEnd('/')}/graphql"
}
