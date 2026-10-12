package com.arrazyfathan.kbbi.feature.detail.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.presentation.ui.UiText
import com.arrazyfathan.kbbi.core.presentation.ui.asUiText
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.usecase.RestoreWordUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WordDetailRouteState(
    val word: ListWordModel? = null,
    val isLoading: Boolean = true,
    val error: UiText? = null,
)

class WordDetailRouteViewModel(
    private val restoreWord: RestoreWordUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(WordDetailRouteState())
    val state = mutableState.asStateFlow()
    private var term: String? = null
    private var loadJob: Job? = null

    fun load(
        word: String,
        retry: Boolean = false,
    ) {
        if (term == word && !retry) return
        term = word
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                mutableState.value = WordDetailRouteState()
                val result =
                    try {
                        restoreWord(word)
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (_: Exception) {
                        AppResult.Error(DataError.Unknown)
                    }
                mutableState.value =
                    when (result) {
                        is AppResult.Success -> {
                            WordDetailRouteState(word = result.data, isLoading = false)
                        }

                        is AppResult.Error -> {
                            WordDetailRouteState(
                                isLoading = false,
                                error = result.error.asUiText(),
                            )
                        }
                    }
            }
    }
}
