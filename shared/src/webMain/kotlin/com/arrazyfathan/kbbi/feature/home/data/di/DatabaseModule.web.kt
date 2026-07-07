package com.arrazyfathan.kbbi.feature.home.data.di

import com.arrazyfathan.kbbi.feature.home.data.source.local.room.LocalStorageWordDao
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.WordDao
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.room.InMemoryProverbDao
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.room.ProverbDao
import org.koin.dsl.module

actual val databaseModule =
    module {
        single<WordDao> { LocalStorageWordDao(get()) }
        single<ProverbDao> { InMemoryProverbDao() }
    }
