package com.petros.efthymiou.dailypulse.sources.data

import kotlinx.serialization.Serializable

@Serializable
data class SourcesGraphqlData(
    val sources: List<SourceRaw> = emptyList(),
)
