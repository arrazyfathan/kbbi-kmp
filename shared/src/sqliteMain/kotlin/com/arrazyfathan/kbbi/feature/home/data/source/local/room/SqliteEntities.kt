package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.CachedTopWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.WordEntity

@Entity(tableName = "word_table")
data class SqliteListWordEntity(
    @PrimaryKey
    val word: String,
    val listWords: List<WordEntity>,
    val visitorCount: Int?,
    val isSaved: Boolean,
    @ColumnInfo(defaultValue = "0")
    val aiGenerated: Boolean = false,
)

@Entity(tableName = "history_table")
data class SqliteHistoryEntity(
    @PrimaryKey
    val word: String,
    val searchedAt: Long,
)

@Entity(tableName = "cached_top_word_table")
data class SqliteTopWordCacheEntity(
    @PrimaryKey
    val word: String,
    val visitorCount: Long,
    val position: Int,
)

fun ListWordEntity.toSqliteEntity() =
    SqliteListWordEntity(
        word = word,
        listWords = listWords,
        visitorCount = visitorCount,
        isSaved = isSaved,
        aiGenerated = aiGenerated,
    )

fun SqliteListWordEntity.toCommonEntity() =
    ListWordEntity(
        word = word,
        listWords = listWords,
        visitorCount = visitorCount,
        isSaved = isSaved,
        aiGenerated = aiGenerated,
    )

fun HistoryEntity.toSqliteEntity() =
    SqliteHistoryEntity(
        word = word,
        searchedAt = searchedAt,
    )

fun SqliteHistoryEntity.toCommonEntity() =
    HistoryEntity(
        word = word,
        searchedAt = searchedAt,
    )

fun CachedTopWordEntity.toSqliteEntity() =
    SqliteTopWordCacheEntity(word = word, visitorCount = visitorCount, position = position)

fun SqliteTopWordCacheEntity.toCommonEntity() =
    CachedTopWordEntity(word = word, visitorCount = visitorCount, position = position)
