package com.petros.efthymiou.dailypulse.aggregators.data

class AggregatorRepository(
    private val service: AggregatorService,
    private val settings: AggregatorSettings
) {

    suspend fun getAggregators(): List<AggregatorRaw> {
        return service.fetchAggregators()
    }

    fun getSelectedAggregatorId(): String? {
        return settings.getSelectedAggregatorId()
    }

    fun saveSelectedAggregatorId(aggregatorId: String) {
        settings.saveSelectedAggregatorId(aggregatorId)
    }
}
