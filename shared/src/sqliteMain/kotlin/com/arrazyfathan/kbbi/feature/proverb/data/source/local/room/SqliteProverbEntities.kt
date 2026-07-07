package com.arrazyfathan.kbbi.feature.proverb.data.source.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbDetailEntity
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbEntity

@Entity(
    tableName = "cached_proverb_table",
    primaryKeys = ["query", "slug"],
)
data class SqliteCachedProverbEntity(
    val query: String,
    val page: Int,
    val position: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val text: String,
    val letter: String,
    val slug: String,
    val sourceUrl: String?,
)

@Entity(tableName = "cached_proverb_detail_table")
data class SqliteCachedProverbDetailEntity(
    @PrimaryKey(autoGenerate = false)
    val slug: String,
    val text: String,
    val letter: String,
    val sourceUrl: String?,
    val meaning: String?,
)

fun CachedProverbEntity.toSqliteEntity() =
    SqliteCachedProverbEntity(
        query = query,
        page = page,
        position = position,
        totalPages = totalPages,
        hasNextPage = hasNextPage,
        text = text,
        letter = letter,
        slug = slug,
        sourceUrl = sourceUrl,
    )

fun SqliteCachedProverbEntity.toCommonEntity() =
    CachedProverbEntity(
        query = query,
        page = page,
        position = position,
        totalPages = totalPages,
        hasNextPage = hasNextPage,
        text = text,
        letter = letter,
        slug = slug,
        sourceUrl = sourceUrl,
    )

fun CachedProverbDetailEntity.toSqliteEntity() =
    SqliteCachedProverbDetailEntity(
        slug = slug,
        text = text,
        letter = letter,
        sourceUrl = sourceUrl,
        meaning = meaning,
    )

fun SqliteCachedProverbDetailEntity.toCommonEntity() =
    CachedProverbDetailEntity(
        slug = slug,
        text = text,
        letter = letter,
        sourceUrl = sourceUrl,
        meaning = meaning,
    )
