@file:Suppress("unused")

package com.petros.efthymiou.dailypulse.platform

import com.petros.efthymiou.dailypulse.articles.presentation.ArticlesState
import com.petros.efthymiou.dailypulse.articles.presentation.ArticlesViewModel
import com.petros.efthymiou.dailypulse.sources.presentation.SourcesState
import com.petros.efthymiou.dailypulse.sources.presentation.SourcesViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Cancel to stop collecting; safe to call more than once from Swift. */
fun interface CloseableFlowCollector {
    fun cancel()
}

fun ArticlesViewModel.collectArticlesStateForSwift(onEach: (ArticlesState) -> Unit): CloseableFlowCollector {
    val job = scope.launch {
        articlesState.collect { onEach(it) }
    }
    return CloseableFlowCollector { job.cancel() }
}

fun SourcesViewModel.collectSourcesStateForSwift(onEach: (SourcesState) -> Unit): CloseableFlowCollector {
    val job = scope.launch {
        sourcesState.collect { onEach(it) }
    }
    return CloseableFlowCollector { job.cancel() }
}
