package com.arrazyfathan.kbbi.feature.home.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.presentation.home.HomeScreen
import com.arrazyfathan.kbbi.navigation.DestinationParser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("home")
data object HomeKey : NavKey

fun EntryProviderScope<NavKey>.homeEntry(
    onNavigateToDetail: (ListWordModel) -> Unit,
    onNavigateToProverb: () -> Unit,
    onNavigateToFigure: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAiSettings: () -> Unit,
    onNavigateToBookmarks: () -> Unit = {},
) {
    entry<HomeKey>(clazzContentKey = DestinationParser::path) {
        HomeRoute(
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToProverb = onNavigateToProverb,
            onNavigateToFigure = onNavigateToFigure,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToAiSettings = onNavigateToAiSettings,
            onNavigateToBookmarks = onNavigateToBookmarks,
        )
    }
}

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (ListWordModel) -> Unit,
    onNavigateToProverb: () -> Unit,
    onNavigateToFigure: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAiSettings: () -> Unit,
    onNavigateToBookmarks: () -> Unit = {},
) {
    HomeScreen(
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToProverb = onNavigateToProverb,
        onNavigateToFigure = onNavigateToFigure,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToAiSettings = onNavigateToAiSettings,
        onNavigateToBookmarks = onNavigateToBookmarks,
        modifier = modifier,
    )
}
