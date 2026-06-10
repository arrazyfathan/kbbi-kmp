package com.arrazyfathan.kbbi.feature.detail.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.kbbi.feature.detail.presentation.detail.DetailScreen
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import kotlinx.serialization.Serializable

@Serializable
data class DetailKey(
    val listWordModel: ListWordModel,
) : NavKey

fun EntryProviderScope<NavKey>.detailEntry() {
    entry<DetailKey> { key ->
        DetailRoute(listWordModel = key.listWordModel)
    }
}

@Composable
fun DetailRoute(listWordModel: ListWordModel) {
    DetailScreen(listWordModel = listWordModel)
}
