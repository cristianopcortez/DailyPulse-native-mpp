package com.petros.efthymiou.dailypulse.articles.data

class ArticlesRepository(
    private val dataSource: ArticlesDataSource,
    private val service: ArticlesService
) {

    suspend fun getArticles(aggregator: String, forceFetch: Boolean): List<ArticleRaw> {
        if (!forceFetch) {
            val articlesDb = dataSource.getAllArticles()
            println("Got ${articlesDb.size} from the database!!")
            if (articlesDb.isNotEmpty()) {
                return articlesDb
            }
        }

        return fetchArticles(aggregator)
    }

    private suspend fun fetchArticles(aggregator: String): List<ArticleRaw> {
        val fetchedArticles = service.fetchArticles(aggregator)
        dataSource.replaceArticles(fetchedArticles)
        return fetchedArticles
    }
}