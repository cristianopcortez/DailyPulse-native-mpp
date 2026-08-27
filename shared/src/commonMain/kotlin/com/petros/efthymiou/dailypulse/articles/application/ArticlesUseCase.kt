package com.petros.efthymiou.dailypulse.articles.application

import com.petros.efthymiou.dailypulse.aggregators.application.AggregatorUseCase
import com.petros.efthymiou.dailypulse.articles.data.ArticleRaw
import com.petros.efthymiou.dailypulse.articles.data.ArticlesRepository
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant

class ArticlesUseCase(
    private val repo: ArticlesRepository,
    private val aggregatorUseCase: AggregatorUseCase
) {

    suspend fun getArticles(forceFetch: Boolean): List<Article> {
        val selectedAggregator = aggregatorUseCase.getSelectedAggregatorId()
        val articlesRaw = repo.getArticles(selectedAggregator, forceFetch)
        return mapArticles(articlesRaw)
    }

    private fun mapArticles(articlesRaw: List<ArticleRaw>): List<Article> = articlesRaw.map { raw ->
        Article(
            raw.title,
            raw.desc.ifBlank { "Click to find out more" },
            getDaysAgoString(raw.date),
            raw.imageUrl.ifBlank {
                "https://image.cnbcfm.com/api/v1/image/107326078-1698758530118-gettyimages-1765623456-wall26362_igj6ehhp.jpeg?v=1698758587&w=1920&h=1080"
            }
        )
    }

    private fun getDaysAgoString(date: String): String {
        val instant = parseInstant(date) ?: return date
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val days = today.daysUntil(
            instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        )

        return when {
            abs(days) > 1 -> "${abs(days)} days ago"
            abs(days) == 1 -> "Yesterday"
            else -> "Today"
        }
    }

    private fun parseInstant(date: String): Instant? =
        runCatching { Instant.parse(date) }.getOrNull()
            ?: runCatching { Instant.parse("${date}Z") }.getOrNull()
}
