package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity

@Database(
    entities = [
        ListWordEntity::class, HistoryEntity::class,
    ],
    version = 8,
    exportSchema = false,
)
@TypeConverters(Converters::class)
@ConstructedBy(WordDatabaseConstructor::class)
abstract class WordDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object WordDatabaseConstructor : RoomDatabaseConstructor<WordDatabase> {
    override fun initialize(): WordDatabase
}


expect fun getDatabaseBuilder(): RoomDatabase.Builder<WordDatabase>

fun getDatabase(builder: RoomDatabase.Builder<WordDatabase>): WordDatabase {
    return builder
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}
