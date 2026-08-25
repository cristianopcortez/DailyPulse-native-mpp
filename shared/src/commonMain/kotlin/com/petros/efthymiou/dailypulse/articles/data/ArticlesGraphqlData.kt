package com.petros.efthymiou.dailypulse.articles.data

import kotlinx.serialization.Serializable

@Serializable
data class ArticlesGraphqlData(
    val articles: List<ArticleRaw> = emptyList(),
)
