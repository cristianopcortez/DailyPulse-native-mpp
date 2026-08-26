package com.petros.efthymiou.dailypulse.articles.data

class ArticlesRepository(
    private val dataSource: ArticlesDataSource,
    private val service: ArticlesService
) {

    suspend fun getArticles(aggregator: String, forceFetch: Boolean): List<ArticleRaw> {
        if (forceFetch) {
            dataSource.clearArticles()
            return fetchArticles(aggregator)
        }

        val articlesDb = dataSource.getAllArticles()
        println("Got ${articlesDb.size} from the database!!")

        if (articlesDb.isEmpty()) {
            return fetchArticles(aggregator)
        }

        return articlesDb
    }

    private suspend fun fetchArticles(aggregator: String): List<ArticleRaw> {
        val fetchedArticles = service.fetchArticles(aggregator)
        dataSource.insertArticles(fetchedArticles)
        return fetchedArticles
    }
}