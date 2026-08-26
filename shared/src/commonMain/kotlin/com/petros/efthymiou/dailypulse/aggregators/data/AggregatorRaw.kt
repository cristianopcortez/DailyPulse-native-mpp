package com.petros.efthymiou.dailypulse.aggregators.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AggregatorRaw(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
)
