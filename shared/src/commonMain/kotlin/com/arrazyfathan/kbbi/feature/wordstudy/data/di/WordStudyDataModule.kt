package com.arrazyfathan.kbbi.feature.wordstudy.data.di

import com.arrazyfathan.kbbi.feature.wordstudy.data.AiConfigurationStore
import com.arrazyfathan.kbbi.feature.wordstudy.data.NetworkWordStudyRepository
import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.BackendWordStudyRemoteDataSource
import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.CustomAiWordStudyRemoteDataSource
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.AiConfigurationRepository
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.WordStudyRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val wordStudyDataModule =
    module {
        single { AiConfigurationStore(get()) }
        single<AiConfigurationRepository> { get<AiConfigurationStore>() }
        singleOf(::BackendWordStudyRemoteDataSource)
        singleOf(::CustomAiWordStudyRemoteDataSource)
        singleOf(::NetworkWordStudyRepository) { bind<WordStudyRepository>() }
    }
