package com.arrazyfathan.kbbi.feature.widgets

import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordCatalogRepository
import com.arrazyfathan.kbbi.widgetcore.WidgetDailyItemSelector
import com.arrazyfathan.kbbi.widgetcore.WidgetKind
import com.arrazyfathan.kbbi.widgetcore.WidgetSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WidgetContentProvider(
    private val wordCatalog: WordCatalogRepository,
    private val bookmarks: BookmarkRepository,
) {
    private val catalogMutex = Mutex()
    private var cachedCatalog: List<String>? = null

    suspend fun catalogWords(): List<String> =
        catalogMutex.withLock {
            cachedCatalog ?: wordCatalog.getWords().also { cachedCatalog = it }
        }

    fun observeSavedWords() = bookmarks.getBookmarks().map { items -> items.map { it.word } }

    suspend fun wordOfDay(
        year: Int,
        dayOfYear: Int,
        localDate: String,
        languageTag: String,
    ): WidgetSnapshot =
        WidgetSnapshot(
            kind = WidgetKind.WORD_OF_DAY,
            localDate = localDate,
            languageTag = languageTag,
            selectedWord =
                WidgetDailyItemSelector.selectWordOfDay(
                    catalogWords(),
                    year,
                    dayOfYear,
                ),
        )

    suspend fun savedWord(
        year: Int,
        dayOfYear: Int,
        localDate: String,
        languageTag: String,
    ): WidgetSnapshot =
        WidgetSnapshot(
            kind = WidgetKind.SAVED_WORD,
            localDate = localDate,
            languageTag = languageTag,
            selectedWord =
                WidgetDailyItemSelector.selectSavedWord(
                    bookmarks.getBookmarks().first().map { it.word },
                    year,
                    dayOfYear,
                ),
        )

    suspend fun savedWord(
        words: List<String>,
        year: Int,
        dayOfYear: Int,
        localDate: String,
        languageTag: String,
    ): WidgetSnapshot =
        WidgetSnapshot(
            kind = WidgetKind.SAVED_WORD,
            localDate = localDate,
            languageTag = languageTag,
            selectedWord = WidgetDailyItemSelector.selectSavedWord(words, year, dayOfYear),
        )
}
