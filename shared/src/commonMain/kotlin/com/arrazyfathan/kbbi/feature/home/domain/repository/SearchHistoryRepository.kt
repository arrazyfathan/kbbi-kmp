package com.arrazyfathan.kbbi.feature.home.domain.repository

import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    suspend fun addToHistory(history: HistoryModel)

    suspend fun clearHistory()

    fun getAllHistories(): Flow<List<HistoryModel>>
}
