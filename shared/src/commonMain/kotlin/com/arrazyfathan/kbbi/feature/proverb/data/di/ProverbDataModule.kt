package com.arrazyfathan.kbbi.feature.proverb.data.di

import com.arrazyfathan.kbbi.feature.proverb.data.NetworkProverbRepository
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.ProverbLocalDataSource
import com.arrazyfathan.kbbi.feature.proverb.data.source.remote.ProverbRemoteDataSource
import com.arrazyfathan.kbbi.feature.proverb.domain.repository.ProverbRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val proverbRepositoryModule =
    module {
        singleOf(::ProverbLocalDataSource)
        singleOf(::ProverbRemoteDataSource)
        singleOf(::NetworkProverbRepository) {
            bind<ProverbRepository>()
        }
    }
