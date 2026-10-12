package com.arrazyfathan.kbbi.feature.proverb.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.kbbi.feature.proverb.presentation.proverb.ProverbRoot
import com.arrazyfathan.kbbi.navigation.DestinationParser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("proverb")
data class ProverbKey(
    val slug: String? = null,
) : NavKey

fun EntryProviderScope<NavKey>.proverbEntry(onNavigateBack: () -> Unit) {
    entry<ProverbKey>(clazzContentKey = DestinationParser::path) { key ->
        ProverbRoute(onNavigateBack = onNavigateBack, slug = key.slug)
    }
}

@Composable
fun ProverbRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    slug: String? = null,
) {
    ProverbRoot(
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        initialSlug = slug,
    )
}
