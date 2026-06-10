@file:Suppress("unused")

package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import androidx.room.TypeConverter
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.WordEntity
import kotlinx.serialization.json.Json

object SqliteConverters {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    @TypeConverter
    fun toListWord(value: String?): List<WordEntity>? = value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromListWord(value: List<WordEntity>?): String? = value?.let { json.encodeToString(it) }
}
