package com.petros.efthymiou.dailypulse.aggregators.di

import com.petros.efthymiou.dailypulse.aggregators.application.AggregatorUseCase
import com.petros.efthymiou.dailypulse.aggregators.data.AggregatorRepository
import com.petros.efthymiou.dailypulse.aggregators.data.AggregatorService
import com.petros.efthymiou.dailypulse.aggregators.data.AggregatorSettings
import com.petros.efthymiou.dailypulse.aggregators.presentation.AggregatorViewModel
import org.koin.dsl.module

val aggregatorModule = module {
    single<AggregatorService> { AggregatorService(get()) }
    single<AggregatorSettings> { AggregatorSettings(get()) }
    single<AggregatorRepository> { AggregatorRepository(get(), get()) }
    single<AggregatorUseCase> { AggregatorUseCase(get()) }
    single<AggregatorViewModel> { AggregatorViewModel(get()) }
}
