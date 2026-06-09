package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

lateinit var appContext: Context

actual fun getDatabaseBuilder(): RoomDatabase.Builder<WordDatabase> {
    return Room.databaseBuilder(
        context = appContext,
        name = "kbbi_db",
        klass = WordDatabase::class.java,
    )
}
