package com.arrazyfathan.kbbi.feature.bookmark.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark.BookmarksScreen
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.navigation.DestinationParser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("bookmarks")
data object BookmarksKey : NavKey

fun EntryProviderScope<NavKey>.bookmarksEntry(onNavigateToDetail: (ListWordModel) -> Unit) {
    entry<BookmarksKey>(clazzContentKey = DestinationParser::path) {
        BookmarkRoute(onNavigateToDetail = onNavigateToDetail)
    }
}

@Composable
fun BookmarkRoute(
    onNavigateToDetail: (ListWordModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    BookmarksScreen(
        onNavigateToDetail = onNavigateToDetail,
        modifier = modifier,
    )
}
