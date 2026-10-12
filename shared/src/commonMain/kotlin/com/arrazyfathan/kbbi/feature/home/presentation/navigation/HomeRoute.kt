package com.arrazyfathan.kbbi.feature.home.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.presentation.home.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : NavKey

fun EntryProviderScope<NavKey>.homeEntry(
    onNavigateToDetail: (ListWordModel) -> Unit,
    onNavigateToProverb: () -> Unit,
    onNavigateToFigure: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    entry<HomeKey> {
        HomeRoute(
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToProverb = onNavigateToProverb,
            onNavigateToFigure = onNavigateToFigure,
            onNavigateToSettings = onNavigateToSettings,
        )
    }
}

@Composable
fun HomeRoute(
    onNavigateToDetail: (ListWordModel) -> Unit,
    onNavigateToProverb: () -> Unit,
    onNavigateToFigure: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeScreen(
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToProverb = onNavigateToProverb,
        onNavigateToFigure = onNavigateToFigure,
        onNavigateToSettings = onNavigateToSettings,
        modifier = modifier,
    )
}
