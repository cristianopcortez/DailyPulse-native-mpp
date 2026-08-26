package com.petros.efthymiou.dailypulse.aggregators.application

import com.petros.efthymiou.dailypulse.aggregators.data.AggregatorRaw
import com.petros.efthymiou.dailypulse.aggregators.data.AggregatorRepository

class AggregatorUseCase(private val repository: AggregatorRepository) {

    companion object {
        const val DEFAULT_AGGREGATOR_ID = "newsapi"
    }

    suspend fun getAggregators(): List<Aggregator> {
        return try {
            val aggregatorsRaw = repository.getAggregators()
            mapAggregators(aggregatorsRaw)
        } catch (e: Exception) {
            println("Failed to fetch aggregators: ${e.message}")
            emptyList()
        }
    }

    fun getSelectedAggregatorId(): String {
        return repository.getSelectedAggregatorId() ?: DEFAULT_AGGREGATOR_ID
    }

    fun selectAggregator(aggregatorId: String) {
        repository.saveSelectedAggregatorId(aggregatorId)
    }

    private fun mapAggregators(aggregatorsRaw: List<AggregatorRaw>): List<Aggregator> =
        aggregatorsRaw.map { raw ->
            Aggregator(
                id = raw.id,
                name = raw.name
            )
        }
}
