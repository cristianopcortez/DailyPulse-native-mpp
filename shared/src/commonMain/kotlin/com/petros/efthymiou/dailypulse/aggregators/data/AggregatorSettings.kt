package com.petros.efthymiou.dailypulse.aggregators.data

import petros.efthymiou.dailypulse.db.DailyPulseDatabase

class AggregatorSettings(private val database: DailyPulseDatabase) {

    fun getSelectedAggregatorId(): String? {
        return database.dailyPulseDatabaseQueries
            .selectAggregatorSetting()
            .executeAsOneOrNull()
            ?.selectedAggregatorId
    }

    fun saveSelectedAggregatorId(aggregatorId: String) {
        database.dailyPulseDatabaseQueries.insertOrReplaceAggregatorSetting(aggregatorId)
    }
}
