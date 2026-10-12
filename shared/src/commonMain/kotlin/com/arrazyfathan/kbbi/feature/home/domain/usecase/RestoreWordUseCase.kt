package com.arrazyfathan.kbbi.feature.home.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordSearchRepository

class RestoreWordUseCase(
    private val repository: WordSearchRepository,
    private val searchWord: SearchWordUseCase,
) {
    suspend operator fun invoke(word: String): AppResult<ListWordModel, DataError> {
        val cached = repository.getCachedWord(word)
        return if (cached != null) {
            AppResult.Success(
                ListWordModel(
                    cached.word,
                    cached.entries,
                    cached.visitorCount,
                    cached.aiGenerated,
                ),
            )
        } else {
            searchWord(word)
        }
    }
}
