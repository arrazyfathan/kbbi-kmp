package com.arrazyfathan.kbbi.feature.figure.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.kbbi.feature.figure.presentation.figure.FigureRoot
import com.arrazyfathan.kbbi.navigation.DestinationParser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("figures")
data object FigureKey : NavKey

fun EntryProviderScope<NavKey>.figureEntry(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
) {
    entry<FigureKey>(clazzContentKey = DestinationParser::path) {
        FigureRoot(onNavigateBack, onNavigateToDetail)
    }
}

@Composable
fun FigureRoute(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FigureRoot(onNavigateBack, onNavigateToDetail, modifier)
}
