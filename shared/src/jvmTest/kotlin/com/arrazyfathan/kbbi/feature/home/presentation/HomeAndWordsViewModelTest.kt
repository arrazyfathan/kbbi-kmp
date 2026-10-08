package com.arrazyfathan.kbbi.feature.home.presentation

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TopWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordResultModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.SearchHistoryRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.TopWordsRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordCatalogRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordSearchRepository
import com.arrazyfathan.kbbi.feature.home.domain.usecase.AddSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetTopWordsUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordEntriesUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordSuggestionsUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ObserveSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SearchWordUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SearchWordWithHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.presentation.home.HomeAction
import com.arrazyfathan.kbbi.feature.home.presentation.home.HomeEvent
import com.arrazyfathan.kbbi.feature.home.presentation.home.HomeSuggestionMode
import com.arrazyfathan.kbbi.feature.home.presentation.home.HomeViewModel
import com.arrazyfathan.kbbi.feature.words.presentation.words.WordListAction
import com.arrazyfathan.kbbi.feature.words.presentation.words.WordListEvent
import com.arrazyfathan.kbbi.feature.words.presentation.words.WordViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeAndWordsViewModelTest {
    private lateinit var mainDispatcher: TestDispatcher

    @BeforeTest
    fun setUp() {
        mainDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun homeLoadsTopWordsAndSearchSuggestions() =
        runTest(mainDispatcher) {
            val viewModel = createHomeViewModel()
            viewModel.onAction(HomeAction.OnStarted)
            advanceUntilIdle()
            viewModel.onAction(HomeAction.OnSearchQueryChanged("ka"))

            assertEquals(
                listOf("kata", "katalog"),
                viewModel.state.value.suggestions
                    .take(2),
            )
            assertEquals(
                "hati",
                viewModel.state.value.topWords
                    .first()
                    .word,
            )
        }

    @Test
    fun selectingSuggestionNavigatesToItsWordDetail() =
        runTest(mainDispatcher) {
            val viewModel = createHomeViewModel()
            viewModel.onAction(HomeAction.OnSuggestionClick("kata"))
            advanceUntilIdle()

            assertEquals(
                "kata",
                assertIs<HomeEvent.NavigateToDetail>(viewModel.events.first()).word.word,
            )
        }

    @Test
    fun notFoundShowsDidYouMeanSuggestionsAndRetryUsesLastQuery() =
        runTest(mainDispatcher) {
            val viewModel = createHomeViewModel()
            viewModel.onAction(HomeAction.OnStarted)
            advanceUntilIdle()
            viewModel.onAction(HomeAction.OnSearchSubmitted("kataa"))
            advanceUntilIdle()

            assertEquals(HomeSuggestionMode.DidYouMean, viewModel.state.value.suggestionMode)
            assertTrue(
                viewModel.state.value.suggestions
                    .contains("kata"),
            )
            viewModel.onAction(HomeAction.OnRetrySearch)
            advanceUntilIdle()
            assertEquals("kataa", viewModel.state.value.searchQuery)
        }

    @Test
    fun newerSearchCancelsOldRequestAndOnlyOpensNewResult() =
        runTest(mainDispatcher) {
            val searchRepository = FakeWordSearchRepository(slowWord = "old")
            val viewModel = createHomeViewModel(searchRepository)
            viewModel.onAction(HomeAction.OnSearchSubmitted("old"))
            runCurrent()
            viewModel.onAction(HomeAction.OnSearchSubmitted("new"))
            advanceUntilIdle()

            assertTrue(searchRepository.cancelledWords.contains("old"))
            assertEquals(
                "new",
                assertIs<HomeEvent.NavigateToDetail>(viewModel.events.first()).word.word,
            )
            assertFalse(viewModel.state.value.isLoading)
        }

    @Test
    fun randomWordSelectsCatalogEntryAndNavigates() =
        runTest(mainDispatcher) {
            val viewModel = createHomeViewModel()
            viewModel.onAction(HomeAction.OnStarted)
            advanceUntilIdle()
            viewModel.onAction(HomeAction.OnRandomWordRequested)
            advanceUntilIdle()

            val openedWord = assertIs<HomeEvent.NavigateToDetail>(viewModel.events.first()).word.word
            assertTrue(openedWord in CATALOG_WORDS)
        }

    @Test
    fun wordListFiltersCaseInsensitivelyAndSelectionNavigates() =
        runTest(mainDispatcher) {
            val viewModel = createWordViewModel()
            viewModel.onAction(WordListAction.OnStarted)
            advanceUntilIdle()
            viewModel.onAction(WordListAction.OnSearchQueryChanged("KA"))

            assertEquals(listOf("kata", "katalog"), viewModel.state.value.filteredWords)
            viewModel.onAction(WordListAction.OnWordClicked("kata"))
            advanceUntilIdle()
            assertEquals(
                "kata",
                assertIs<WordListEvent.NavigateToDetail>(viewModel.events.first()).word.word,
            )
        }

    private fun createHomeViewModel(repository: FakeWordSearchRepository = FakeWordSearchRepository()) =
        HomeViewModel(
            searchWordWithHistory =
                SearchWordWithHistoryUseCase(
                    SearchWordUseCase(repository),
                    AddSearchHistoryUseCase(FakeHistoryRepository()),
                ),
            observeSearchHistory = ObserveSearchHistoryUseCase(FakeHistoryRepository()),
            getWordEntries = GetWordEntriesUseCase(FakeWordCatalogRepository()),
            getWordSuggestions = GetWordSuggestionsUseCase(),
            getTopWords = GetTopWordsUseCase(FakeTopWordsRepository()),
        )

    private fun createWordViewModel() =
        WordViewModel(
            searchWord = SearchWordUseCase(FakeWordSearchRepository()),
            getWordEntries = GetWordEntriesUseCase(FakeWordCatalogRepository()),
        )

    private class FakeWordSearchRepository(
        private val slowWord: String? = null,
    ) : WordSearchRepository {
        val cancelledWords = mutableSetOf<String>()

        override suspend fun getMeaningOfWord(word: String): AppResult<WordResultModel, DataError> {
            if (word == "kataa") return AppResult.Error(DataError.NotFound)
            try {
                if (word == slowWord) delay(10_000)
            } finally {
                if (word == slowWord) cancelledWords += word
            }
            return AppResult.Success(WordResultModel(word, emptyList()))
        }
    }

    private class FakeHistoryRepository : SearchHistoryRepository {
        override suspend fun addToHistory(history: HistoryModel) = Unit

        override suspend fun clearHistory() = Unit

        override fun getAllHistories(): Flow<List<HistoryModel>> = emptyFlow()
    }

    private class FakeWordCatalogRepository : WordCatalogRepository {
        override suspend fun getWords() = CATALOG_WORDS
    }

    private class FakeTopWordsRepository : TopWordsRepository {
        override suspend fun getTopWords(limit: Int): AppResult<List<TopWordModel>, DataError> =
            AppResult.Success(listOf(TopWordModel("hati", 12)))

        override suspend fun getCachedTopWords(limit: Int): List<TopWordModel> = emptyList()
    }

    private companion object {
        val CATALOG_WORDS = listOf("kata", "katalog", "bahasa")
    }
}
