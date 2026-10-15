package com.arrazyfathan.kbbi.feature.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.platform.LocaleProvider
import com.arrazyfathan.kbbi.core.platform.currentWidgetLocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

data class GlanceableEntryState(
    val dailyWord: String? = null,
    val savedWord: String? = null,
    val isDailyLoading: Boolean = true,
    val isSavedLoading: Boolean = true,
    val dailyHasError: Boolean = false,
    val savedHasError: Boolean = false,
)

class GlanceableEntryViewModel(
    private val provider: WidgetContentProvider,
    private val localeProvider: LocaleProvider,
) : ViewModel() {
    private val mutableState = MutableStateFlow(GlanceableEntryState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                refreshDaily()
                delay(1.minutes)
            }
        }
        viewModelScope.launch {
            try {
                provider.observeSavedWords().collect { words ->
                    val date = currentWidgetLocalDate()
                    val locale =
                        localeProvider.currentLocaleTag().substringBefore('-').ifBlank { "en" }
                    val saved =
                        provider.savedWord(
                            words,
                            date.year,
                            date.dayOfYear,
                            date.toIsoString(),
                            locale,
                        )
                    mutableState.update {
                        it.copy(
                            savedWord = saved.selectedWord,
                            isSavedLoading = false,
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                mutableState.update { it.copy(isSavedLoading = false, savedHasError = true) }
            }
        }
    }

    private suspend fun refreshDaily() {
        try {
            val date = currentWidgetLocalDate()
            val locale = localeProvider.currentLocaleTag().substringBefore('-').ifBlank { "en" }
            val daily = provider.wordOfDay(date.year, date.dayOfYear, date.toIsoString(), locale)
            mutableState.update {
                it.copy(
                    dailyWord = daily.selectedWord,
                    isDailyLoading = false,
                    dailyHasError = false,
                )
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            mutableState.update { it.copy(isDailyLoading = false, dailyHasError = true) }
        }
    }
}
