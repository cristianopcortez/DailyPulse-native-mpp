package com.petros.efthymiou.dailypulse.sources.application

import com.petros.efthymiou.dailypulse.aggregators.application.AggregatorUseCase
import com.petros.efthymiou.dailypulse.sources.data.SourceRaw
import com.petros.efthymiou.dailypulse.sources.data.SourcesRepository

class SourcesUseCase(
    private val repo: SourcesRepository,
    private val aggregatorUseCase: AggregatorUseCase
) {

    suspend fun getSources(): List<Source> {
        val selectedAggregator = aggregatorUseCase.getSelectedAggregatorId()
        val sourcesRaw = repo.getAllSources(selectedAggregator)

        return mapSources(sourcesRaw)
    }

    fun clearSources() {
        repo.clearSources()
    }

    private fun mapSources(sourcesRaw: List<SourceRaw>): List<Source> = sourcesRaw.map { raw ->
        Source(
            raw.id,
            raw.name,
            raw.desc,
            raw.origin,
        )
    }
}
