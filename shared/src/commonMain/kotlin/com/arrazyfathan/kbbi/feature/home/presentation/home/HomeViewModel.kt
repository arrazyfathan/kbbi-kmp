package com.arrazyfathan.kbbi.feature.home.presentation.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.presentation.ui.UiText
import com.arrazyfathan.kbbi.core.presentation.ui.asUiText
import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TopWordModel
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetTopWordsUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordEntriesUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordSuggestionsUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ObserveSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SearchWordWithHistoryUseCase
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.error_random_word_unavailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TOP_WORDS_LIMIT = 5

@Immutable
data class HomeState(
    val searchQuery: String = "",
    val histories: List<HistoryModel> = emptyList(),
    val topWords: List<TopWordUi> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val suggestionMode: HomeSuggestionMode = HomeSuggestionMode.Search,
    val isLoading: Boolean = false,
    val isTopWordsLoading: Boolean = false,
    val isSearchFocused: Boolean = false,
    val searchError: UiText? = null,
    val topWordsError: UiText? = null,
)

data class TopWordUi(
    val rank: Int,
    val word: String,
)

enum class HomeSuggestionMode {
    Search,
    DidYouMean,
}

sealed interface HomeAction {
    data object OnStarted : HomeAction

    data class OnSearchQueryChanged(
        val query: String,
    ) : HomeAction

    data class OnSearchFocusChanged(
        val isFocused: Boolean,
    ) : HomeAction

    data class OnSearchSubmitted(
        val word: String,
    ) : HomeAction

    data class OnSuggestionClick(
        val word: String,
    ) : HomeAction

    data class OnTopWordClick(
        val word: String,
    ) : HomeAction

    data object OnRandomWordRequested : HomeAction

    data object OnRetrySearch : HomeAction

    data object OnRetryTopWords : HomeAction
}

sealed interface HomeEvent {
    data class NavigateToDetail(
        val word: ListWordModel,
    ) : HomeEvent

    data class ShowMessage(
        val message: UiText,
    ) : HomeEvent
}

class HomeViewModel(
    private val searchWordWithHistory: SearchWordWithHistoryUseCase,
    private val observeSearchHistory: ObserveSearchHistoryUseCase,
    private val getWordEntries: GetWordEntriesUseCase,
    private val getWordSuggestions: GetWordSuggestionsUseCase,
    private val getTopWords: GetTopWordsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var historiesJob: Job? = null
    private var wordEntriesJob: Job? = null
    private var topWordsJob: Job? = null
    private var topWordsRequestId = 0L
    private var searchJob: Job? = null
    private var searchRequestId = 0L
    private var wordEntries: List<String> = emptyList()
    private var lastSearchQuery: String = ""

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnStarted -> {
                observeHistories()
                loadWordEntries()
                loadTopWords()
            }

            is HomeAction.OnSearchQueryChanged -> {
                updateSearchQuery(action.query)
            }

            is HomeAction.OnSearchFocusChanged -> {
                _state.update { it.copy(isSearchFocused = action.isFocused) }
            }

            is HomeAction.OnSearchSubmitted -> {
                search(action.word)
            }

            is HomeAction.OnSuggestionClick -> {
                search(action.word)
            }

            is HomeAction.OnTopWordClick -> {
                search(action.word)
            }

            HomeAction.OnRandomWordRequested -> {
                searchRandomWord()
            }

            HomeAction.OnRetrySearch -> {
                lastSearchQuery.takeIf(String::isNotBlank)?.let(::search)
            }

            HomeAction.OnRetryTopWords -> {
                loadTopWords(force = true)
            }
        }
    }

    private fun observeHistories() {
        if (historiesJob != null) return
        historiesJob =
            viewModelScope.launch {
                observeSearchHistory().collect { histories ->
                    _state.update { it.copy(histories = histories) }
                }
            }
    }

    private fun loadWordEntries() {
        if (wordEntriesJob?.isActive == true) return
        wordEntriesJob =
            viewModelScope.launch {
                try {
                    val entries = getWordEntries()
                    wordEntries = entries
                    _state.update { current ->
                        current.copy(suggestions = getWordSuggestions(current.searchQuery, entries))
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    wordEntries = emptyList()
                }
            }
    }

    private fun loadTopWords(force: Boolean = false) {
        if (!force && topWordsJob?.isActive == true) return
        topWordsJob?.cancel()
        val requestId = ++topWordsRequestId
        topWordsJob =
            viewModelScope.launch {
                _state.update { it.copy(isTopWordsLoading = true, topWordsError = null) }
                try {
                    val cached = getTopWords.cached(limit = TOP_WORDS_LIMIT)
                    if (requestId != topWordsRequestId) return@launch
                    if (cached.isNotEmpty()) _state.update { it.copy(topWords = cached.toTopWordUi()) }

                    val result = getTopWords(limit = TOP_WORDS_LIMIT)
                    if (requestId != topWordsRequestId) return@launch
                    when (result) {
                        is AppResult.Success -> {
                            _state.update {
                                it.copy(topWords = result.data.take(TOP_WORDS_LIMIT).toTopWordUi())
                            }
                        }

                        is AppResult.Error -> {
                            _state.update { it.copy(topWordsError = result.error.asUiText()) }
                        }
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (exception: Exception) {
                    if (requestId == topWordsRequestId) {
                        _state.update { it.copy(topWordsError = UiText.DynamicString(exception.message.orEmpty())) }
                    }
                } finally {
                    if (requestId == topWordsRequestId) _state.update { it.copy(isTopWordsLoading = false) }
                }
            }
    }

    private fun updateSearchQuery(query: String) {
        val normalizedQuery = query.trimStart()
        _state.update {
            it.copy(
                searchQuery = normalizedQuery,
                suggestions = getWordSuggestions(normalizedQuery, wordEntries),
                suggestionMode = HomeSuggestionMode.Search,
                searchError = null,
            )
        }
    }

    private fun searchRandomWord() {
        if (wordEntries.isEmpty()) loadWordEntries()
        viewModelScope.launch {
            val entries =
                wordEntries.ifEmpty {
                    try {
                        getWordEntries()
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (_: Exception) {
                        emptyList()
                    }
                }
            val randomWord = entries.randomOrNull()
            if (randomWord == null) {
                _events.send(HomeEvent.ShowMessage(UiText.StringResource(Res.string.error_random_word_unavailable)))
            } else {
                search(randomWord)
            }
        }
    }

    private fun search(word: String) {
        val normalizedWord = word.trim()
        if (normalizedWord.isBlank()) return
        lastSearchQuery = normalizedWord
        val requestId = ++searchRequestId
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                _state.update {
                    it.copy(
                        searchQuery = normalizedWord,
                        suggestions = emptyList(),
                        searchError = null,
                        isLoading = true,
                    )
                }
                try {
                    val result =
                        try {
                            searchWordWithHistory(normalizedWord)
                        } catch (cancellation: CancellationException) {
                            throw cancellation
                        } catch (_: Exception) {
                            AppResult.Error(DataError.Unknown)
                        }
                    if (requestId != searchRequestId) return@launch
                    when (result) {
                        is AppResult.Success -> {
                            _state.update { it.copy(searchQuery = "", searchError = null) }
                            _events.send(HomeEvent.NavigateToDetail(result.data))
                        }

                        is AppResult.Error -> {
                            val suggestions =
                                if (result.error == DataError.NotFound) {
                                    getWordSuggestions(normalizedWord, wordEntries)
                                } else {
                                    emptyList()
                                }
                            _state.update {
                                it.copy(
                                    suggestions = suggestions,
                                    suggestionMode =
                                        if (result.error ==
                                            DataError.NotFound
                                        ) {
                                            HomeSuggestionMode.DidYouMean
                                        } else {
                                            HomeSuggestionMode.Search
                                        },
                                    searchError = result.error.asUiText(),
                                )
                            }
                            _events.send(HomeEvent.ShowMessage(result.error.asUiText()))
                        }
                    }
                } finally {
                    if (requestId == searchRequestId) _state.update { it.copy(isLoading = false) }
                }
            }
    }

    private fun List<TopWordModel>.toTopWordUi(): List<TopWordUi> =
        mapIndexed { index, topWord -> TopWordUi(rank = index + 1, word = topWord.word) }
}
