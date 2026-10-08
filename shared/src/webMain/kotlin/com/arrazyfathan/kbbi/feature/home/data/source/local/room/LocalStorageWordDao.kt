package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.CachedTopWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import web.storage.localStorage

class LocalStorageWordDao(
    private val json: Json,
) : WordDao {
    private val words = MutableStateFlow(readBookmarks())
    private val histories = MutableStateFlow(readHistories())

    override fun getAllWords(): Flow<List<ListWordEntity>> = words

    override fun getSavedWords(): Flow<List<ListWordEntity>> = words.map { current -> current.filter { it.isSaved } }

    override suspend fun getWord(word: String): ListWordEntity? =
        words.value.firstOrNull { it.word.trim().equals(word.trim(), ignoreCase = true) }

    override suspend fun insertWord(listWordEntity: ListWordEntity): Long {
        val updatedWords =
            words.value.filterNot { it.word.equals(listWordEntity.word, ignoreCase = true) } + listWordEntity

        if (!write(BOOKMARKS_KEY, updatedWords)) return -1L

        words.value = updatedWords
        return 1L
    }

    override suspend fun deleteWord(word: String): Int {
        val currentWords = words.value
        val updatedWords = currentWords.filterNot { it.word.equals(word, ignoreCase = true) }
        val deletedCount = currentWords.size - updatedWords.size

        if (deletedCount == 0 || !write(BOOKMARKS_KEY, updatedWords)) return 0

        words.value = updatedWords
        return deletedCount
    }

    override fun checkWordIsSaved(word: String): Flow<Boolean> =
        words.map { current -> current.any { it.isSaved && it.word.trim().equals(word.trim(), ignoreCase = true) } }

    override suspend fun insertHistory(historyEntity: HistoryEntity) {
        val updatedHistories =
            histories.value.filterNot { it.word.equals(historyEntity.word, ignoreCase = true) } + historyEntity

        if (write(HISTORIES_KEY, updatedHistories)) {
            histories.value = updatedHistories
        }
    }

    override suspend fun trimHistories(limit: Int) {
        val updatedHistories =
            histories.value.sortedWith(compareByDescending<HistoryEntity> { it.searchedAt }.thenByDescending { it.word })
                .take(limit)

        if (write(HISTORIES_KEY, updatedHistories)) {
            histories.value = updatedHistories
        }
    }

    override fun getListHistory(): Flow<List<HistoryEntity>> =
        histories.map { current ->
            current.sortedWith(compareByDescending<HistoryEntity> { it.searchedAt }.thenByDescending { it.word })
        }

    override suspend fun clearHistory() {
        if (write(HISTORIES_KEY, emptyList<HistoryEntity>())) histories.value = emptyList()
    }

    override suspend fun getTopWords(): List<CachedTopWordEntity> =
        runCatching {
            localStorage.getItem(TOP_WORDS_KEY)?.let { json.decodeFromString<List<CachedTopWordEntity>>(it) }
        }.getOrNull().orEmpty().sortedBy { it.position }

    override suspend fun replaceTopWords(topWords: List<CachedTopWordEntity>) {
        write(TOP_WORDS_KEY, topWords)
    }

    private fun readBookmarks(): List<ListWordEntity> =
        runCatching {
            localStorage.getItem(BOOKMARKS_KEY)?.let { json.decodeFromString<List<ListWordEntity>>(it) }
        }.getOrNull().orEmpty()

    private fun readHistories(): List<HistoryEntity> =
        runCatching {
            localStorage.getItem(HISTORIES_KEY)?.let { json.decodeFromString<List<HistoryEntity>>(it) }
        }.getOrNull().orEmpty()

    private inline fun <reified T> write(
        key: String,
        value: T,
    ): Boolean =
        runCatching {
            localStorage.setItem(key, json.encodeToString(value))
        }.isSuccess

    private companion object {
        const val BOOKMARKS_KEY = "kbbi.bookmarks.v1"
        const val HISTORIES_KEY = "kbbi.search-history.v1"
        const val TOP_WORDS_KEY = "kbbi.top-words.v1"
    }
}
