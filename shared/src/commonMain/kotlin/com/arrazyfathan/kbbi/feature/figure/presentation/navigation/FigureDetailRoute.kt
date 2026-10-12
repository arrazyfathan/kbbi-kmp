package com.arrazyfathan.kbbi.feature.figure.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.kbbi.feature.figure.presentation.figure.FigureDetailRoot
import com.arrazyfathan.kbbi.navigation.DestinationParser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("figure")
data class FigureDetailKey(
    val slug: String,
) : NavKey

fun EntryProviderScope<NavKey>.figureDetailEntry(onNavigateBack: () -> Unit) {
    entry<FigureDetailKey>(clazzContentKey = DestinationParser::path) { key ->
        FigureDetailRoute(key.slug, onNavigateBack)
    }
}

@Composable
fun FigureDetailRoute(
    slug: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FigureDetailRoot(slug, onNavigateBack, modifier)
}
