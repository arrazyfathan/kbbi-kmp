package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.WordEntity

@Entity(tableName = "word_table")
data class SqliteListWordEntity(
    @PrimaryKey
    val word: String,
    val listWords: List<WordEntity>,
    val isSaved: Boolean,
)

@Entity(tableName = "history_table")
data class SqliteHistoryEntity(
    @PrimaryKey
    val word: String,
    val searchedAt: Long,
)

fun ListWordEntity.toSqliteEntity() =
    SqliteListWordEntity(
        word = word,
        listWords = listWords,
        isSaved = isSaved,
    )

fun SqliteListWordEntity.toCommonEntity() =
    ListWordEntity(
        word = word,
        listWords = listWords,
        isSaved = isSaved,
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
