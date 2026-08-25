package com.petros.efthymiou.dailypulse.articles.presentation

import com.petros.efthymiou.dailypulse.BaseViewModel
import com.petros.efthymiou.dailypulse.articles.application.ArticlesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArticlesViewModel(
    private val useCase: ArticlesUseCase
) : BaseViewModel() {

    private val _articlesState: MutableStateFlow<ArticlesState> =
        MutableStateFlow(ArticlesState(loading = true))

    val articlesState: StateFlow<ArticlesState> get() = _articlesState

    init {
        getArticles()
    }

    fun getArticles(forceFetch: Boolean = false) {
        scope.launch {
            _articlesState.emit(ArticlesState(loading = true, articles = _articlesState.value.articles))

            try {
                val fetchedArticles = useCase.getArticles(forceFetch)
                _articlesState.emit(ArticlesState(articles = fetchedArticles))
            } catch (e: Exception) {
                _articlesState.emit(
                    ArticlesState(
                        articles = _articlesState.value.articles,
                        error = e.message?.takeIf { it.isNotBlank() } ?: "Unable to load data",
                    )
                )
            }
        }
    }
}
