package com.arrazyfathan.kbbi.feature.words.presentation.words

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.presentation.ui.UiText
import com.arrazyfathan.kbbi.core.presentation.ui.asUiText
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordEntriesUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SearchWordUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ar Razy Fathan Rabbani on 18/03/23.
 */
@Immutable
data class WordListState(
    val searchQuery: String = "",
    val words: List<String> = emptyList(),
    val filteredWords: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val loadError: Boolean = false,
)

sealed interface WordListAction {
    data object OnStarted : WordListAction

    data object OnRetryLoad : WordListAction

    data class OnSearchQueryChanged(
        val query: String,
    ) : WordListAction

    data class OnWordClicked(
        val word: String,
    ) : WordListAction
}

sealed interface WordListEvent {
    data class NavigateToDetail(
        val word: ListWordModel,
    ) : WordListEvent

    data class ShowMessage(
        val message: UiText,
    ) : WordListEvent
}

class WordViewModel(
    private val searchWord: SearchWordUseCase,
    private val getWordEntries: GetWordEntriesUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(WordListState())
    val state = _state.asStateFlow()

    private val _events = Channel<WordListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var searchJob: Job? = null
    private var wordsJob: Job? = null
    private var searchRequestId = 0L

    fun onAction(action: WordListAction) {
        when (action) {
            WordListAction.OnStarted -> {
                loadWords()
            }

            WordListAction.OnRetryLoad -> {
                loadWords(force = true)
            }

            is WordListAction.OnSearchQueryChanged -> {
                _state.update {
                    it.copy(
                        searchQuery = action.query,
                        filteredWords = filterWords(it.words, action.query),
                    )
                }
            }

            is WordListAction.OnWordClicked -> {
                search(action.word)
            }
        }
    }

    private fun loadWords(force: Boolean = false) {
        if (!force && wordsJob?.isActive == true) return
        wordsJob?.cancel()
        wordsJob =
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true, loadError = false) }
                try {
                    val words = getWordEntries()
                    _state.update {
                        it.copy(
                            words = words,
                            filteredWords = filterWords(words, it.searchQuery),
                            loadError = false,
                        )
                    }
                } catch (cancellation: kotlinx.coroutines.CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    _state.update { it.copy(loadError = true) }
                } finally {
                    _state.update { it.copy(isLoading = false) }
                }
            }
    }

    private fun search(word: String) {
        val requestId = ++searchRequestId
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                try {
                    val result =
                        try {
                            searchWord(word)
                        } catch (cancellation: CancellationException) {
                            throw cancellation
                        } catch (_: Exception) {
                            AppResult.Error(DataError.Unknown)
                        }
                    if (requestId != searchRequestId) return@launch
                    when (result) {
                        is AppResult.Success -> _events.send(WordListEvent.NavigateToDetail(result.data))
                        is AppResult.Error -> _events.send(WordListEvent.ShowMessage(result.error.asUiText()))
                    }
                } finally {
                    if (requestId == searchRequestId) _state.update { it.copy(isLoading = false) }
                }
            }
    }

    private fun filterWords(
        words: List<String>,
        query: String,
    ): List<String> =
        if (query.isEmpty()) {
            words
        } else {
            words.filter { it.contains(query, ignoreCase = true) }
        }
}
