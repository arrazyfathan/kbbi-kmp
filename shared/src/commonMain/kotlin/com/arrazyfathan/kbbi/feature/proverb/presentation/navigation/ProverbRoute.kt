package com.arrazyfathan.kbbi.feature.proverb.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.kbbi.feature.proverb.presentation.proverb.ProverbRoot
import kotlinx.serialization.Serializable

@Serializable
data object ProverbKey : NavKey

fun EntryProviderScope<NavKey>.proverbEntry(onNavigateBack: () -> Unit) {
    entry<ProverbKey> {
        ProverbRoute(onNavigateBack = onNavigateBack)
    }
}

@Composable
fun ProverbRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProverbRoot(
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}
