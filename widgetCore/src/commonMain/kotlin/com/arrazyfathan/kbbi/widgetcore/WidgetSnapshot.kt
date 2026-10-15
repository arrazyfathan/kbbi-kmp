package com.arrazyfathan.kbbi.widgetcore

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.random.Random

@Serializable
enum class WidgetKind {
    WORD_OF_DAY,
    SAVED_WORD,
    QUICK_SEARCH,
}

/** Small, versioned payload shared by native widgets and foreground glanceable surfaces. */
@Serializable
data class WidgetSnapshot(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val kind: WidgetKind,
    /** Gregorian local date in ISO-8601 form, supplied by the host clock. */
    val localDate: String,
    val languageTag: String,
    val selectedWord: String? = null,
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

/** Canonical source-compatible day selection. Catalog order is significant. */
object WidgetDailyItemSelector {
    fun selectWordOfDay(
        words: List<String>,
        year: Int,
        dayOfYear: Int,
    ): String? = select(words.filter(String::isNotBlank), year, dayOfYear)

    fun selectSavedWord(
        words: List<String>,
        year: Int,
        dayOfYear: Int,
    ): String? = select(words.filter(String::isNotBlank).sorted(), year, dayOfYear)

    fun <T> select(
        items: List<T>,
        year: Int,
        dayOfYear: Int,
    ): T? =
        items
            .takeIf(List<T>::isNotEmpty)
            ?.get(Random(year * DAYS_PER_LEAP_YEAR + dayOfYear).nextInt(items.size))

    fun wordOfDaySnapshotJson(
        words: List<String>,
        year: Int,
        dayOfYear: Int,
        localDate: String,
        languageTag: String,
    ): String =
        encode(
            WidgetSnapshot(
                kind = WidgetKind.WORD_OF_DAY,
                localDate = localDate,
                languageTag = languageTag,
                selectedWord = selectWordOfDay(words, year, dayOfYear),
            ),
        )

    fun savedWordSnapshotJson(
        words: List<String>,
        year: Int,
        dayOfYear: Int,
        localDate: String,
        languageTag: String,
    ): String =
        encode(
            WidgetSnapshot(
                kind = WidgetKind.SAVED_WORD,
                localDate = localDate,
                languageTag = languageTag,
                selectedWord = selectSavedWord(words, year, dayOfYear),
            ),
        )

    fun quickSearchSnapshotJson(
        localDate: String,
        languageTag: String,
    ): String =
        encode(
            WidgetSnapshot(
                kind = WidgetKind.QUICK_SEARCH,
                localDate = localDate,
                languageTag = languageTag,
            ),
        )

    fun decodeSnapshot(json: String): WidgetSnapshot? =
        runCatching { snapshotJson.decodeFromString<WidgetSnapshot>(json) }.getOrNull()?.takeIf {
            it.schemaVersion == WidgetSnapshot.CURRENT_SCHEMA_VERSION && it.localDate.length == 10 && it.localDate[4] == '-' &&
                it.localDate[7] == '-' &&
                it.languageTag in SUPPORTED_LANGUAGES &&
                (it.selectedWord == null || it.selectedWord.length <= MAX_WORD_LENGTH)
        }

    private fun encode(snapshot: WidgetSnapshot): String = snapshotJson.encodeToString(snapshot)

    private val snapshotJson = Json { encodeDefaults = true }
    private val SUPPORTED_LANGUAGES = setOf("en", "id", "in")
    private const val DAYS_PER_LEAP_YEAR = 366
    private const val MAX_WORD_LENGTH = 256
}
