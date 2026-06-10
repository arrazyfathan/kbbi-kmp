package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<WordDatabase> {
    val databaseFile = File(System.getProperty("user.home"), ".kbbi/kbbi_db.db")
    databaseFile.parentFile.mkdirs()
    return Room.databaseBuilder<WordDatabase>(
        name = databaseFile.absolutePath,
        factory = { WordDatabaseConstructor.initialize() },
    ).setDriver(BundledSQLiteDriver())
}
