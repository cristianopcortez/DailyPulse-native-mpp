package com.petros.efthymiou.dailypulse.di

import com.petros.efthymiou.dailypulse.articles.presentation.ArticlesViewModel
import com.petros.efthymiou.dailypulse.network.TestBffConfig
import com.petros.efthymiou.dailypulse.sources.presentation.SourcesViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import petros.efthymiou.dailypulse.db.DailyPulseDatabase

fun initKoin() {

    val modules = sharedKoinModules + databaseModule

    startKoin {
        modules(modules)
    }

    if (TestBffConfig.isUiTesting()) {
        clearCachedDataForUiTests()
    }
}

private object UiTestDatabase : KoinComponent {
    val instance: DailyPulseDatabase by inject()
}

private fun clearCachedDataForUiTests() {
    UiTestDatabase.instance.dailyPulseDatabaseQueries.removeAllArticles()
    UiTestDatabase.instance.dailyPulseDatabaseQueries.removeAllSources()
}

class ArticlesInjector : KoinComponent {

    val articlesViewModel: ArticlesViewModel by inject()
}

class SourcesInjector : KoinComponent {

    val sourcesViewModel: SourcesViewModel by inject()
}