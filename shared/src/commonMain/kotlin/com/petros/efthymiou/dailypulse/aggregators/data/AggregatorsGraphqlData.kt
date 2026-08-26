package com.petros.efthymiou.dailypulse.aggregators.data

import kotlinx.serialization.Serializable

@Serializable
data class AggregatorsGraphqlData(
    val aggregators: List<AggregatorRaw> = emptyList(),
)
