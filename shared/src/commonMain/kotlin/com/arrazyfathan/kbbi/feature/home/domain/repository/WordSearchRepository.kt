package com.arrazyfathan.kbbi.feature.home.domain.repository

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.WordResultModel

interface WordSearchRepository {
    /** Cached data for restoring a route without repeating a remote lookup. */
    suspend fun getCachedWord(word: String): WordResultModel? = null

    suspend fun getMeaningOfWord(word: String): AppResult<WordResultModel, DataError>
}
