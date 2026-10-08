package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.CachedTopWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */

interface WordDao : StorageIssueSource {
    override fun getStorageIssues(): Flow<List<StorageIssue>> = emptyFlow()

    fun getAllWords(): Flow<List<ListWordEntity>>

    fun getSavedWords(): Flow<List<ListWordEntity>>

    suspend fun getWord(word: String): ListWordEntity?

    suspend fun insertWord(listWordEntity: ListWordEntity): Long

    suspend fun deleteWord(word: String): Int

    fun checkWordIsSaved(word: String): Flow<Boolean>

    suspend fun insertHistory(historyEntity: HistoryEntity)

    suspend fun trimHistories(limit: Int)

    suspend fun insertHistoryAndTrim(
        historyEntity: HistoryEntity,
        limit: Int = 5,
    ) {
        insertHistory(historyEntity)
        trimHistories(limit)
    }

    fun getListHistory(): Flow<List<HistoryEntity>>

    suspend fun clearHistory()

    suspend fun getTopWords(): List<CachedTopWordEntity>

    suspend fun replaceTopWords(topWords: List<CachedTopWordEntity>)
}
