package com.petros.efthymiou.dailypulse.sources.presentation

import com.petros.efthymiou.dailypulse.BaseViewModel
import com.petros.efthymiou.dailypulse.sources.application.SourcesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SourcesViewModel(private val useCase: SourcesUseCase) : BaseViewModel() {

    private val _sourcesState =
        MutableStateFlow(SourcesState(listOf(), true, null))
    val sourcesState: StateFlow<SourcesState> get() = _sourcesState

    init {
        getSources()
    }

    private fun getSources() {
        scope.launch {
            _sourcesState.emit(SourcesState(_sourcesState.value.sources, true, null))

            try {
                val sources = useCase.getSources()
                _sourcesState.emit(SourcesState(sources))
            } catch (e: Exception) {
                _sourcesState.emit(
                    SourcesState(
                        sources = _sourcesState.value.sources,
                        loading = false,
                        error = e.message?.takeIf { it.isNotBlank() } ?: "Unable to load data",
                    )
                )
            }
        }
    }
}
