package com.arrazyfathan.kbbi.feature.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.observability.AnalyticsEvent
import com.arrazyfathan.kbbi.core.observability.AnalyticsReporter
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.widget_data_unavailable
import kbbi_kmp.shared.generated.resources.widget_empty_daily
import kbbi_kmp.shared.generated.resources.widget_empty_saved
import kbbi_kmp.shared.generated.resources.widget_loading
import kbbi_kmp.shared.generated.resources.widget_saved_word
import kbbi_kmp.shared.generated.resources.widget_section_title
import kbbi_kmp.shared.generated.resources.widget_word_of_day
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.arrazyfathan.kbbi.core.observability.WidgetKind as AnalyticsWidgetKind

@Composable
fun GlanceableEntrySection(
    onSearch: (String) -> Unit,
    onQuickSearch: () -> Unit,
    onOpenBookmarks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GlanceableEntryViewModel = koinViewModel()
    val analytics: AnalyticsReporter = koinInject()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.widget_section_title),
            color = Color.White,
            fontSize = 16.sp,
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            WidgetCard(
                title = stringResource(Res.string.widget_word_of_day),
                word = state.dailyWord,
                emptyLabel =
                    stringResource(
                        when {
                            state.isDailyLoading -> Res.string.widget_loading
                            state.dailyHasError -> Res.string.widget_data_unavailable
                            else -> Res.string.widget_empty_daily
                        },
                    ),
                background = Color.White.copy(alpha = 0.16f),
                textColor = Color.White,
                onClick = {
                    analytics.log(AnalyticsEvent.WidgetOpened(AnalyticsWidgetKind.WordOfDay))
                    state.dailyWord?.let(onSearch) ?: onQuickSearch()
                },
                modifier = Modifier.weight(1f),
            )
            WidgetCard(
                title = stringResource(Res.string.widget_saved_word),
                word = state.savedWord,
                emptyLabel =
                    stringResource(
                        when {
                            state.isSavedLoading -> Res.string.widget_loading
                            state.savedHasError -> Res.string.widget_data_unavailable
                            else -> Res.string.widget_empty_saved
                        },
                    ),
                background = Color(0xFFFFE9A8),
                textColor = Color(0xFF302506),
                onClick = {
                    analytics.log(AnalyticsEvent.WidgetOpened(AnalyticsWidgetKind.SavedWord))
                    state.savedWord?.let(onSearch) ?: onOpenBookmarks()
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun WidgetCard(
    title: String,
    word: String?,
    emptyLabel: String,
    background: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = background),
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                title,
                color = textColor,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
            )
            Text(
                text = word?.takeIf(String::isNotBlank) ?: emptyLabel,
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
        }
    }
}
