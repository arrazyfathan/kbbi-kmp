package com.arrazyfathan.kbbi.feature.home.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TopWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslateModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordResultModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.SearchHistoryRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.TopWordsRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.TranslateRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeUseCasesTest {
    @Test
    fun suggestionsRankPrefixesThenFuzzyCandidatesAndRespectLimit() {
        val suggestions = GetWordSuggestionsUseCase()(
            query = "kita",
            words = listOf("kata", "kito", "batu"),
            maxSuggestions = 2,
        )
        assertEquals(listOf("kata", "kito"), suggestions)
    }

    @Test
    fun translationTrimsWordAndRejectsBlankInput() = runTest {
        var queried = ""
        val useCase = GetWordTranslationUseCase(object : TranslateRepository {
            override suspend fun getTranslation(word: String): AppResult<TranslateModel, DataError> {
                queried = word
                return AppResult.Success(translation)
            }
        })
        assertEquals(AppResult.Success(translation), useCase(" kata "))
        assertEquals("kata", queried)
        assertEquals(AppResult.Error(DataError.EmptyQuery), useCase("  "))
        assertEquals("kata", queried)
    }

    @Test
    fun topWordsNormalizesRequestedLimit() = runTest {
        var requestedLimit = 0
        val useCase = GetTopWordsUseCase(object : TopWordsRepository {
            override suspend fun getTopWords(limit: Int): AppResult<List<TopWordModel>, DataError> {
                requestedLimit = limit
                return AppResult.Success(listOf(topWord))
            }

            override suspend fun getCachedTopWords(limit: Int) = emptyList<TopWordModel>()
        })
        assertEquals(AppResult.Success(listOf(topWord)), useCase(500))
        assertEquals(100, requestedLimit)
    }

    @Test
    fun searchUseCasePreservesVisitorMetadataAndAiFlag() = runTest {
        var queried = ""
        val useCase = SearchWordUseCase(object : WordSearchRepository {
            override suspend fun getMeaningOfWord(word: String): AppResult<WordResultModel, DataError> {
                queried = word
                return AppResult.Success(
                    WordResultModel("kata", listOf(WordModel("kata", emptyList())), visitorCount = 18, aiGenerated = true),
                )
            }
        })
        val result = useCase(" kata ") as AppResult.Success
        assertEquals("kata", queried)
        assertEquals(18, result.data.visitorCount)
        assertEquals(true, result.data.aiGenerated)
    }

    @Test
    fun clearHistoryDelegatesToRepository() = runTest {
        var cleared = false
        val useCase = ClearSearchHistoryUseCase(object : SearchHistoryRepository {
            override suspend fun addToHistory(history: HistoryModel) = Unit
            override suspend fun clearHistory() { cleared = true }
            override fun getAllHistories(): Flow<List<HistoryModel>> = emptyFlow()
        })
        useCase()
        assertEquals(true, cleared)
    }

    private companion object {
        val translation = TranslateModel("kata", "word", "id", "en", "provider", emptyList())
        val topWord = TopWordModel("kata", 18L)
    }
}
