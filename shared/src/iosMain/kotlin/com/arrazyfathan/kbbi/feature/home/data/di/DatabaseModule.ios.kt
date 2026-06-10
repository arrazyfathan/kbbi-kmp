package com.arrazyfathan.kbbi.feature.home.data.di

import com.arrazyfathan.kbbi.feature.home.data.source.local.room.RoomWordDaoAdapter
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.WordDao
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.WordDatabase
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.getDatabase
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.getDatabaseBuilder
import org.koin.dsl.module

actual val databaseModule =
    module {
        factory<WordDao> { RoomWordDaoAdapter(get<WordDatabase>().wordDao()) }
        single { getDatabase(getDatabaseBuilder()) }
    }
