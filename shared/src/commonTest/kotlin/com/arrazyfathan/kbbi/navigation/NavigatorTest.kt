package com.arrazyfathan.kbbi.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigatorTest {
    @Test
    fun switchingTopLevelRoutesPreservesEachBackStack() {
        val state = createNavigationState()
        val navigator = Navigator(state)

        navigator.navigate(DetailsKey("home"))
        navigator.navigate(SavedKey)
        navigator.navigate(DetailsKey("saved"))
        navigator.navigate(HomeKey)

        assertEquals(listOf<NavKey>(HomeKey, DetailsKey("home")), state.backStacks.getValue(HomeKey).toList())
        assertEquals(listOf<NavKey>(SavedKey, DetailsKey("saved")), state.backStacks.getValue(SavedKey).toList())
        assertEquals(HomeKey, state.topLevelRoute)
    }

    @Test
    fun backPopsCurrentRouteBeforeReturningToStartRoute() {
        val state = createNavigationState()
        val navigator = Navigator(state)

        navigator.navigate(SavedKey)
        navigator.navigate(DetailsKey("saved"))
        navigator.goBack()

        assertEquals(listOf<NavKey>(SavedKey), state.backStacks.getValue(SavedKey).toList())
        assertEquals(SavedKey, state.topLevelRoute)

        navigator.goBack()

        assertEquals(HomeKey, state.topLevelRoute)
    }

    @Test
    fun restoringSnapshotRestoresTopLevelRouteAndEveryBackStack() {
        val state = createNavigationState()
        val navigator = Navigator(state)

        navigator.navigate(DetailsKey("home"))
        val homeDetailsSnapshot = state.snapshot()

        navigator.navigate(SavedKey)
        navigator.navigate(DetailsKey("saved"))
        state.restore(homeDetailsSnapshot)

        assertEquals(HomeKey, state.topLevelRoute)
        assertEquals(listOf<NavKey>(HomeKey, DetailsKey("home")), state.backStacks.getValue(HomeKey).toList())
        assertEquals(listOf<NavKey>(SavedKey), state.backStacks.getValue(SavedKey).toList())
    }

    private fun createNavigationState(): NavigationState {
        val homeStack = NavBackStack<NavKey>(HomeKey)
        val savedStack = NavBackStack<NavKey>(SavedKey)

        return NavigationState(
            startRoute = HomeKey,
            topLevelRoute = mutableStateOf(HomeKey),
            backStacks =
                mapOf(
                    HomeKey to homeStack,
                    SavedKey to savedStack,
                ),
        )
    }

    @Serializable
    private data object HomeKey : NavKey

    @Serializable
    private data object SavedKey : NavKey

    @Serializable
    private data class DetailsKey(
        val id: String,
    ) : NavKey
}
