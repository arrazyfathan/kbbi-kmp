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

fun EntryProviderScope<NavKey>.homeEntry(onNavigateToDetail: (ListWordModel) -> Unit) {
    entry<HomeKey> {
        HomeRoute(onNavigateToDetail = onNavigateToDetail)
    }
}

@Composable
fun HomeRoute(
    onNavigateToDetail: (ListWordModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeScreen(
        onNavigateToDetail = onNavigateToDetail,
        modifier = modifier,
    )
}
