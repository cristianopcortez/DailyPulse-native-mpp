package com.petros.efthymiou.dailypulse.aggregators.presentation

import com.petros.efthymiou.dailypulse.aggregators.application.Aggregator

data class AggregatorState(
    val aggregators: List<Aggregator> = emptyList(),
    val selectedAggregatorId: String = "",
    val loading: Boolean = false,
    val error: String? = null
)
