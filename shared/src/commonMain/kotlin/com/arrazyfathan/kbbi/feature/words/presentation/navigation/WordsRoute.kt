package com.arrazyfathan.kbbi.feature.words.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import com.arrazyfathan.kbbi.navigation.DestinationParser
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.words.presentation.words.WordListScreen
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("words")
data object WordsKey : NavKey

fun EntryProviderScope<NavKey>.wordsEntry(onNavigateToDetail: (ListWordModel) -> Unit) {
    entry<WordsKey>(clazzContentKey = DestinationParser::path) {
        WordsRoute(onNavigateToDetail = onNavigateToDetail)
    }
}

@Composable
fun WordsRoute(
    onNavigateToDetail: (ListWordModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    WordListScreen(
        onNavigateToDetail = onNavigateToDetail,
        modifier = modifier,
    )
}
