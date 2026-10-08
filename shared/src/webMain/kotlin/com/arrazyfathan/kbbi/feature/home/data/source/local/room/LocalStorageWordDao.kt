package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.CachedTopWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import web.storage.localStorage

class LocalStorageWordDao(
    private val json: Json,
    private val storage: WordStorage = BrowserWordStorage,
) : WordDao {
    private val mutableStorageIssues = MutableStateFlow<List<StorageIssue>>(emptyList())
    private val words = MutableStateFlow(readBookmarks())
    private val histories = MutableStateFlow(readHistories())

    override fun getStorageIssues(): Flow<List<StorageIssue>> = mutableStorageIssues.asStateFlow()

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
        val matchingWords = currentWords.filter { it.word.trim().equals(word.trim(), ignoreCase = true) && it.isSaved }
        val updatedWords =
            currentWords.map { cachedWord ->
                if (cachedWord in matchingWords) cachedWord.copy(isSaved = false) else cachedWord
            }

        if (matchingWords.isEmpty() || !write(BOOKMARKS_KEY, updatedWords)) return 0

        words.value = updatedWords
        return matchingWords.size
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

    override suspend fun insertHistoryAndTrim(historyEntity: HistoryEntity, limit: Int) {
        val updatedHistories =
            (histories.value.filterNot { it.word.equals(historyEntity.word, ignoreCase = true) } + historyEntity)
                .sortedWith(compareByDescending<HistoryEntity> { it.searchedAt }.thenByDescending { it.word })
                .take(limit.coerceAtLeast(0))

        if (write(HISTORIES_KEY, updatedHistories)) histories.value = updatedHistories
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
        read<List<CachedTopWordEntity>>(TOP_WORDS_KEY).orEmpty().sortedBy { it.position }

    override suspend fun replaceTopWords(topWords: List<CachedTopWordEntity>) {
        write(TOP_WORDS_KEY, topWords)
    }

    private fun readBookmarks(): List<ListWordEntity> =
        read<List<ListWordEntity>>(BOOKMARKS_KEY).orEmpty()

    private fun readHistories(): List<HistoryEntity> =
        read<List<HistoryEntity>>(HISTORIES_KEY).orEmpty()

    private inline fun <reified T> read(key: String): T? =
        try {
            storage.getItem(key)?.let { json.decodeFromString<T>(it) }
        } catch (exception: Exception) {
            recordIssue(key, StorageOperation.Read, exception)
            null
        }

    private inline fun <reified T> write(
        key: String,
        value: T,
    ): Boolean =
        runCatching {
            storage.setItem(key, json.encodeToString(value))
        }.fold(
            onSuccess = { true },
            onFailure = { exception ->
                recordIssue(key, StorageOperation.Write, exception)
                false
            },
        )

    private fun recordIssue(key: String, operation: StorageOperation, exception: Throwable) {
        mutableStorageIssues.value =
            (mutableStorageIssues.value + StorageIssue(key, operation, exception.message ?: exception::class.simpleName.orEmpty()))
                .takeLast(MAX_STORAGE_ISSUES)
    }

    private object BrowserWordStorage : WordStorage {
        override fun getItem(key: String): String? = localStorage.getItem(key)

        override fun setItem(key: String, value: String) {
            localStorage.setItem(key, value)
        }
    }

    private companion object {
        const val BOOKMARKS_KEY = "kbbi.bookmarks.v1"
        const val HISTORIES_KEY = "kbbi.search-history.v1"
        const val TOP_WORDS_KEY = "kbbi.top-words.v1"
        const val MAX_STORAGE_ISSUES = 10
    }
}
