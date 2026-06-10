package com.arrazyfathan.kbbi.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.arrazyfathan.kbbi.feature.bookmark.presentation.navigation.BookmarksKey
import com.arrazyfathan.kbbi.feature.detail.presentation.navigation.DetailKey
import com.arrazyfathan.kbbi.feature.home.presentation.navigation.HomeKey
import com.arrazyfathan.kbbi.feature.words.presentation.navigation.WordsKey
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.bookmarks_title
import kbbi_kmp.shared.generated.resources.home
import kbbi_kmp.shared.generated.resources.home_selected
import kbbi_kmp.shared.generated.resources.home_title
import kbbi_kmp.shared.generated.resources.saved
import kbbi_kmp.shared.generated.resources.saved_selected
import kbbi_kmp.shared.generated.resources.word
import kbbi_kmp.shared.generated.resources.word_list_tab_title
import kbbi_kmp.shared.generated.resources.word_selected
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

internal data class TopLevelDestination(
    val key: NavKey,
    val title: StringResource,
    val icon: DrawableResource,
    val selectedIcon: DrawableResource,
)

internal val topLevelDestinations =
    listOf(
        TopLevelDestination(
            key = HomeKey,
            title = Res.string.home_title,
            icon = Res.drawable.home,
            selectedIcon = Res.drawable.home_selected,
        ),
        TopLevelDestination(
            key = WordsKey,
            title = Res.string.word_list_tab_title,
            icon = Res.drawable.word,
            selectedIcon = Res.drawable.word_selected,
        ),
        TopLevelDestination(
            key = BookmarksKey,
            title = Res.string.bookmarks_title,
            icon = Res.drawable.saved,
            selectedIcon = Res.drawable.saved_selected,
        ),
    )

internal val topLevelRoutes = topLevelDestinations.map(TopLevelDestination::key)

internal val navigationSavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(HomeKey::class, HomeKey.serializer())
                    subclass(WordsKey::class, WordsKey.serializer())
                    subclass(BookmarksKey::class, BookmarksKey.serializer())
                    subclass(DetailKey::class, DetailKey.serializer())
                }
            }
    }
