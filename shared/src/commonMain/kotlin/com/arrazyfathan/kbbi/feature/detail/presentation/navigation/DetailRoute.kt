package com.arrazyfathan.kbbi.feature.detail.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.kbbi.feature.detail.presentation.detail.DetailScreen
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.navigation.DestinationParser
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.navigate_back
import kbbi_kmp.shared.generated.resources.retry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Serializable
@SerialName("word")
data class DetailKey(
    val word: String,
) : NavKey

fun EntryProviderScope<NavKey>.detailEntry(
    onNavigateBack: () -> Unit,
    onNavigateToAiSettings: () -> Unit,
) {
    entry<DetailKey>(clazzContentKey = DestinationParser::path) { key ->
        WordDetailRoot(
            key.word,
            onNavigateBack,
            onNavigateToAiSettings,
        )
    }
}

@Composable
private fun WordDetailRoot(
    word: String,
    onNavigateBack: () -> Unit,
    onNavigateToAiSettings: () -> Unit,
) {
    val viewModel: WordDetailRouteViewModel = koinViewModel(key = "word-detail:$word")
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(word) { viewModel.load(word) }
    val loadedWord = state.word
    if (loadedWord != null) {
        DetailRoute(loadedWord, onNavigateToAiSettings)
    } else {
        Column(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.isLoading) CircularProgressIndicator()
            state.error?.let { error ->
                Text(error.asString())
                TextButton(onClick = { viewModel.load(word, retry = true) }) {
                    Text(stringResource(Res.string.retry))
                }
            }
            TextButton(onClick = onNavigateBack) { Text(stringResource(Res.string.navigate_back)) }
        }
    }
}

@Composable
fun DetailRoute(
    listWordModel: ListWordModel,
    onNavigateToAiSettings: () -> Unit = {},
) {
    DetailScreen(listWordModel = listWordModel, onNavigateToAiSettings = onNavigateToAiSettings)
}
