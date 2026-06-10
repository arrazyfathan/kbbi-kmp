package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class InMemoryWordDao : WordDao {
    private val words = MutableStateFlow<List<ListWordEntity>>(emptyList())
    private val histories = MutableStateFlow<List<HistoryEntity>>(emptyList())

    override fun getAllWords(): Flow<List<ListWordEntity>> = words

    override suspend fun insertWord(listWordEntity: ListWordEntity): Long {
        words.update { current ->
            current.filterNot { it.word.equals(listWordEntity.word, ignoreCase = true) } + listWordEntity
        }
        return 1L
    }

    override suspend fun deleteWord(word: String): Int {
        val initialSize = words.value.size
        words.update { current -> current.filterNot { it.word.equals(word, ignoreCase = true) } }
        return initialSize - words.value.size
    }

    override fun checkWordIsExist(word: String): Flow<Boolean> =
        words.map { current -> current.any { it.word.equals(word, ignoreCase = true) } }

    override suspend fun insertHistory(historyEntity: HistoryEntity) {
        histories.update { current ->
            current.filterNot { it.word.equals(historyEntity.word, ignoreCase = true) } + historyEntity
        }
    }

    override suspend fun trimHistories(limit: Int) {
        histories.update { current ->
            current.sortedWith(compareByDescending<HistoryEntity> { it.searchedAt }.thenByDescending { it.word })
                .take(limit)
        }
    }

    override fun getListHistory(): Flow<List<HistoryEntity>> =
        histories.map { current ->
            current.sortedWith(compareByDescending<HistoryEntity> { it.searchedAt }.thenByDescending { it.word })
        }
}
