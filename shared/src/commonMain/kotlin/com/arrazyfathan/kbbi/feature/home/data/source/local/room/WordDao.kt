package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */

interface WordDao {
    fun getAllWords(): Flow<List<ListWordEntity>>

    suspend fun insertWord(listWordEntity: ListWordEntity): Long

    suspend fun deleteWord(word: String): Int

    fun checkWordIsExist(word: String): Flow<Boolean>

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
}
