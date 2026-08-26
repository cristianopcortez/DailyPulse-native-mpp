package com.petros.efthymiou.dailypulse.aggregators.presentation

import com.petros.efthymiou.dailypulse.BaseViewModel
import com.petros.efthymiou.dailypulse.aggregators.application.AggregatorUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AggregatorViewModel(
    private val useCase: AggregatorUseCase
) : BaseViewModel() {

    private val _aggregatorState: MutableStateFlow<AggregatorState> =
        MutableStateFlow(AggregatorState(loading = true))

    val aggregatorState: StateFlow<AggregatorState> get() = _aggregatorState

    init {
        loadAggregators()
    }

    private fun loadAggregators() {
        scope.launch {
            _aggregatorState.emit(AggregatorState(loading = true))

            try {
                val aggregators = useCase.getAggregators()
                val selectedId = useCase.getSelectedAggregatorId()

                _aggregatorState.emit(
                    AggregatorState(
                        aggregators = aggregators,
                        selectedAggregatorId = selectedId
                    )
                )
            } catch (e: Exception) {
                _aggregatorState.emit(
                    AggregatorState(
                        error = e.message?.takeIf { it.isNotBlank() } ?: "Unable to load aggregators"
                    )
                )
            }
        }
    }

    fun selectAggregator(aggregatorId: String) {
        useCase.selectAggregator(aggregatorId)
        _aggregatorState.value = _aggregatorState.value.copy(selectedAggregatorId = aggregatorId)
    }
}
