package com.arrazyfathan.kbbi.feature.home.data.di

import com.arrazyfathan.kbbi.feature.home.data.source.local.room.RoomWordDaoAdapter
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.WordDao
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.WordDatabase
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.getDatabase
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.getDatabaseBuilder
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.room.ProverbDao
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.room.RoomProverbDaoAdapter
import org.koin.dsl.module

actual val databaseModule =
    module {
        factory<WordDao> { RoomWordDaoAdapter(get<WordDatabase>().wordDao()) }
        factory<ProverbDao> { RoomProverbDaoAdapter(get<WordDatabase>().proverbDao()) }
        single { getDatabase(getDatabaseBuilder()) }
    }
