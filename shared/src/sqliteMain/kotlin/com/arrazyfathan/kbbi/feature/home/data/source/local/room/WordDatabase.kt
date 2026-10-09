package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.room.RoomProverbDao
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.room.SqliteCachedProverbDetailEntity
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.room.SqliteCachedProverbEntity

@Database(
    entities = [
        SqliteListWordEntity::class,
        SqliteHistoryEntity::class,
        SqliteTopWordCacheEntity::class,
        SqliteCachedProverbEntity::class,
        SqliteCachedProverbDetailEntity::class,
    ],
    version = 12,
    exportSchema = false,
)
@TypeConverters(SqliteConverters::class)
@ConstructedBy(WordDatabaseConstructor::class)
abstract class WordDatabase : RoomDatabase() {
    abstract fun wordDao(): RoomWordDao

    abstract fun proverbDao(): RoomProverbDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object WordDatabaseConstructor : RoomDatabaseConstructor<WordDatabase> {
    override fun initialize(): WordDatabase
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<WordDatabase>

fun getDatabase(builder: RoomDatabase.Builder<WordDatabase>): WordDatabase =
    builder
        .addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

private val MIGRATION_9_10 =
    object : Migration(9, 10) {
        override fun migrate(connection: SQLiteConnection) {
            connection
                .prepare("ALTER TABLE word_table ADD COLUMN visitorCount INTEGER")
                .use { statement -> statement.step() }
        }
    }

private val MIGRATION_10_11 =
    object : Migration(10, 11) {
        override fun migrate(connection: SQLiteConnection) {
            connection
                .prepare("ALTER TABLE word_table ADD COLUMN aiGenerated INTEGER NOT NULL DEFAULT 0")
                .use { statement -> statement.step() }
            connection
                .prepare(
                    "CREATE TABLE IF NOT EXISTS cached_top_word_table (" + "word TEXT NOT NULL PRIMARY KEY, " +
                        "visitorCount INTEGER NOT NULL, " +
                        "position INTEGER NOT NULL)",
                ).use { statement -> statement.step() }
        }
    }

private val MIGRATION_11_12 =
    object : Migration(11, 12) {
        override fun migrate(connection: SQLiteConnection) {
            connection
                .prepare("ALTER TABLE cached_proverb_detail_table ADD COLUMN aiGenerated INTEGER NOT NULL DEFAULT 0")
                .use { statement -> statement.step() }
            connection
                .prepare("ALTER TABLE cached_proverb_detail_table ADD COLUMN notice TEXT")
                .use { statement -> statement.step() }
        }
    }
