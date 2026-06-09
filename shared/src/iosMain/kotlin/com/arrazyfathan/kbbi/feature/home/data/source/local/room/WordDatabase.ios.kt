package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun getDatabaseBuilder(): RoomDatabase.Builder<WordDatabase> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    val dbFilePath = documentDirectory!!.path!! + "/kbbi_db.db"
    return Room.databaseBuilder<WordDatabase>(
        name = dbFilePath,
        factory = { WordDatabaseConstructor.initialize() }  // KMP-safe: resolved post-KSP
    ).setDriver(BundledSQLiteDriver())
}
