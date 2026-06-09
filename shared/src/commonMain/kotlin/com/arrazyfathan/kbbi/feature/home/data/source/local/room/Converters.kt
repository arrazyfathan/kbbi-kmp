@file:Suppress("unused")

package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import androidx.room.TypeConverter
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.MeaningEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.WordEntity
import kotlinx.serialization.json.Json

object Converters {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    @TypeConverter
    fun toWordList(string: String?): List<ListWordEntity>? =
        if (string != null) {
            json.decodeFromString<List<ListWordEntity>>(string)
        } else {
            null
        }

    @TypeConverter
    fun toListWord(string: String?): List<WordEntity>? =
        if (string != null) {
            json.decodeFromString<List<WordEntity>>(string)
        } else {
            null
        }

    @TypeConverter
    fun fromListWord(list: List<WordEntity>?): String? =
        if (list != null) {
            json.encodeToString(list)
        } else {
            null
        }

    @TypeConverter
    fun fromWordList(list: List<ListWordEntity>?): String? =
        if (list != null) {
            json.encodeToString(list)
        } else {
            null
        }

    @TypeConverter
    fun toListMeanings(string: String?): List<MeaningEntity>? =
        if (string != null) {
            json.decodeFromString<List<MeaningEntity>>(string)
        } else {
            null
        }

    @TypeConverter
    fun fromListMeanings(list: List<MeaningEntity>?): String? =
        if (list != null) {
            json.encodeToString(list)
        } else {
            null
        }
}
