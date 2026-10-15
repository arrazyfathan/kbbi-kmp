package com.arrazyfathan.kbbi.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.kbbi.feature.bookmark.presentation.navigation.BookmarksKey
import com.arrazyfathan.kbbi.feature.detail.presentation.navigation.DetailKey
import com.arrazyfathan.kbbi.feature.home.presentation.navigation.HomeKey
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigatorTest {
    @Test
    fun switchingTopLevelRoutesPreservesEachBackStack() {
        val state = createNavigationState()
        val navigator = Navigator(state)

        navigator.navigate(DetailKey("home"))
        navigator.navigate(BookmarksKey)
        navigator.navigate(DetailKey("saved"))
        navigator.navigate(HomeKey)

        assertEquals(listOf<NavKey>(HomeKey, DetailKey("home")), state.backStacks.getValue(HomeKey).toList())
        assertEquals(listOf<NavKey>(BookmarksKey, DetailKey("saved")), state.backStacks.getValue(BookmarksKey).toList())
        assertEquals(HomeKey, state.topLevelRoute)
    }

    @Test
    fun backPopsCurrentRouteBeforeReturningToStartRoute() {
        val state = createNavigationState()
        val navigator = Navigator(state)

        navigator.navigate(BookmarksKey)
        navigator.navigate(DetailKey("saved"))
        navigator.goBack()

        assertEquals(listOf<NavKey>(BookmarksKey), state.backStacks.getValue(BookmarksKey).toList())
        assertEquals(BookmarksKey, state.topLevelRoute)

        navigator.goBack()

        assertEquals(HomeKey, state.topLevelRoute)
    }

    @Test
    fun restoringSnapshotRestoresTopLevelRouteAndEveryBackStack() {
        val state = createNavigationState()
        val navigator = Navigator(state)

        navigator.navigate(DetailKey("home"))
        val homeDetailsSnapshot = state.snapshot()

        navigator.navigate(BookmarksKey)
        navigator.navigate(DetailKey("saved"))
        state.restore(homeDetailsSnapshot)

        assertEquals(HomeKey, state.topLevelRoute)
        assertEquals(listOf<NavKey>(HomeKey, DetailKey("home")), state.backStacks.getValue(HomeKey).toList())
        assertEquals(listOf<NavKey>(BookmarksKey), state.backStacks.getValue(BookmarksKey).toList())
    }

    private fun createNavigationState(): NavigationState {
        val backStacks = topLevelRoutes.associateWith { route -> NavBackStack<NavKey>(route) }

        return NavigationState(
            startRoute = HomeKey,
            topLevelRoute = mutableStateOf(HomeKey),
            backStacks = backStacks,
        )
    }
}
