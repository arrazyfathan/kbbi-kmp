package com.arrazyfathan.kbbi.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.arrazyfathan.kbbi.BindSystemBarColor
import com.arrazyfathan.kbbi.core.presentation.designsystem.components.AppLottieAnimation
import com.arrazyfathan.kbbi.core.presentation.ui.LocalAppLoadingController
import com.arrazyfathan.kbbi.core.presentation.ui.rememberAppLoadingController
import com.arrazyfathan.kbbi.feature.bookmark.presentation.navigation.BookmarkRoute
import com.arrazyfathan.kbbi.feature.detail.presentation.navigation.DetailRoute
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.presentation.navigation.HomeRoute
import com.arrazyfathan.kbbi.feature.words.presentation.navigation.WordsRoute
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
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource

private const val IOS_NAVIGATION_TRANSITION_DURATION_MILLIS = 350
private const val IOS_NAVIGATION_PARALLAX_DIVISOR = 3
private const val BOTTOM_NAVIGATION_TRANSITION_DURATION_MILLIS = 220

sealed interface Screen : NavKey {
    val title: StringResource
    val icon: DrawableResource
    val iconSelected: DrawableResource

    @Serializable
    data object Home : Screen {
        override val title = Res.string.home_title
        override val icon = Res.drawable.home
        override val iconSelected = Res.drawable.home_selected
    }

    @Serializable
    data object WordList : Screen {
        override val title = Res.string.word_list_tab_title
        override val icon = Res.drawable.word
        override val iconSelected = Res.drawable.word_selected
    }

    @Serializable
    data object Bookmarks : Screen {
        override val title = Res.string.bookmarks_title
        override val icon = Res.drawable.saved
        override val iconSelected = Res.drawable.saved_selected
    }
}

@Serializable
private data class DetailNavRoute(
    val dataJson: String,
) : NavKey

@Composable
fun MainApp() {
    val screens =
        listOf(
            Screen.Home,
            Screen.WordList,
            Screen.Bookmarks,
        )
    val navigationState =
        rememberNavigationState(
            startRoute = Screen.Home,
            topLevelRoutes = screens,
        )
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val currentRoute = navigationState.currentRoute
    val isDetailVisible = currentRoute is DetailNavRoute
    val loadingController = rememberAppLoadingController()
    val routeJson =
        remember {
            Json {
                ignoreUnknownKeys = true
            }
        }
    val isUiBlocked by remember {
        derivedStateOf { loadingController.isBlocking }
    }

    BindSystemBarColor(isDetailVisible = isDetailVisible)

    val entries =
        navigationState.toEntries(
            entryProvider {
                entry<Screen.Home> {
                    HomeRoute(
                        onNavigateToDetail = { word ->
                            navigator.navigate(DetailNavRoute(routeJson.encodeToString(word)))
                        },
                    )
                }
                entry<Screen.WordList> {
                    WordsRoute(
                        onNavigateToDetail = { word ->
                            navigator.navigate(DetailNavRoute(routeJson.encodeToString(word)))
                        },
                    )
                }
                entry<Screen.Bookmarks> {
                    BookmarkRoute(
                        onNavigateToDetail = { word ->
                            navigator.navigate(DetailNavRoute(routeJson.encodeToString(word)))
                        },
                    )
                }
                entry<DetailNavRoute> { route ->
                    val listWordModel =
                        remember(route.dataJson) {
                            routeJson.decodeFromString<ListWordModel>(route.dataJson)
                        }
                    DetailRoute(listWordModel = listWordModel)
                }
            },
        )

    CompositionLocalProvider(LocalAppLoadingController provides loadingController) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                entries = entries,
                onBack = {
                    if (!isUiBlocked) {
                        navigator.goBack()
                    }
                },
                modifier = Modifier.fillMaxSize(),
                transitionSpec = { appNavigationTransition(isDetailVisible) },
                popTransitionSpec = { appPopNavigationTransition(isDetailVisible) },
                predictivePopTransitionSpec = { appPopNavigationTransition(isDetailVisible) },
            )

            if (!isDetailVisible) {
                Row(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(70.dp)
                            .shadow(elevation = 16.dp)
                            .background(Color.White),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    screens.forEach { screen ->
                        val isSelected = navigationState.topLevelRoute == screen
                        Box(
                            modifier =
                                Modifier.weight(1f).fillMaxHeight().clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = false, radius = 24.dp),
                                ) {
                                    if (!isUiBlocked && !isSelected) {
                                        navigator.navigate(screen)
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter =
                                    painterResource(
                                        resource = if (isSelected) screen.iconSelected else screen.icon,
                                    ),
                                contentDescription = null,
                                tint = Color.Unspecified,
                            )
                        }
                    }
                }
            }

            if (isUiBlocked) {
                BlockingLoadingOverlay()
            }
        }
    }
}

@Composable
private fun BlockingLoadingOverlay() {
    Box(
        modifier =
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            AppLottieAnimation(
                assetPath = "files/loading_search.json",
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private val navigationSavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            kotlinx.serialization.modules.SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Screen.Home::class, Screen.Home.serializer())
                    subclass(Screen.WordList::class, Screen.WordList.serializer())
                    subclass(Screen.Bookmarks::class, Screen.Bookmarks.serializer())
                    subclass(DetailNavRoute::class, DetailNavRoute.serializer())
                }
            }
    }

@Composable
private fun rememberCustomNavBackStack(startRoute: NavKey): NavBackStack<NavKey> {
    val routeJson =
        remember {
            Json {
                ignoreUnknownKeys = true
                serializersModule = navigationSavedStateConfiguration.serializersModule
            }
        }

    val saver =
        remember(routeJson) {
            Saver<NavBackStack<NavKey>, String>(
                save = { backStack ->
                    val list = backStack.toList()
                    routeJson.encodeToString(
                        ListSerializer(PolymorphicSerializer(NavKey::class)),
                        list,
                    )
                },
                restore = { jsonStr ->
                    val list =
                        routeJson.decodeFromString(
                            ListSerializer(PolymorphicSerializer(NavKey::class)),
                            jsonStr,
                        )
                    val backStack = NavBackStack(list.first())
                    if (list.size > 1) {
                        backStack.addAll(list.drop(1))
                    }
                    backStack
                },
            )
        }

    return rememberSaveable(saver = saver) {
        NavBackStack(startRoute)
    }
}

@Composable
private fun rememberNavigationState(
    startRoute: Screen,
    topLevelRoutes: List<Screen>,
): NavigationState {
    val topLevelRoute =
        remember {
            mutableStateOf<NavKey>(startRoute)
        }

    val backStacks: Map<NavKey, NavBackStack<NavKey>> =
        topLevelRoutes.associate { key ->
            key to rememberCustomNavBackStack(key)
        }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks,
        )
    }
}

private class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    var topLevelRoute: NavKey by topLevelRoute

    val currentRoute: NavKey
        get() = backStacks[topLevelRoute]?.lastOrNull() ?: topLevelRoute

    val stacksInUse: List<NavKey>
        get() =
            if (topLevelRoute == startRoute) {
                listOf(startRoute)
            } else {
                listOf(startRoute, topLevelRoute)
            }
}

private class Navigator(
    private val state: NavigationState,
) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute] ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}

@Composable
private fun NavigationState.toEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): SnapshotStateList<NavEntry<NavKey>> {
    val decoratedEntries =
        backStacks.mapValues { (_, stack) ->
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators =
                    listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        // rememberViewModelStoreNavEntryDecorator(),
                    ),
                entryProvider = entryProvider,
            )
        }

    return stacksInUse.flatMap { decoratedEntries[it] ?: emptyList() }.toMutableStateList()
}

private fun appNavigationTransition(isDetailVisible: Boolean): ContentTransform =
    if (isDetailVisible) {
        iosNavigationTransition()
    } else {
        bottomNavigationTransition()
    }

private fun appPopNavigationTransition(isDetailVisible: Boolean): ContentTransform =
    if (isDetailVisible) {
        iosPopNavigationTransition()
    } else {
        bottomNavigationTransition()
    }

private fun bottomNavigationTransition(): ContentTransform {
    val animationSpec = tween<Float>(durationMillis = BOTTOM_NAVIGATION_TRANSITION_DURATION_MILLIS)

    return fadeIn(animationSpec = animationSpec) togetherWith fadeOut(animationSpec = animationSpec)
}

private fun iosNavigationTransition(): ContentTransform {
    val animationSpec = tween<IntOffset>(durationMillis = IOS_NAVIGATION_TRANSITION_DURATION_MILLIS)

    return slideInHorizontally(
        animationSpec = animationSpec,
        initialOffsetX = { fullWidth -> fullWidth },
    ) togetherWith
        slideOutHorizontally(
            animationSpec = animationSpec,
            targetOffsetX = { fullWidth -> -fullWidth / IOS_NAVIGATION_PARALLAX_DIVISOR },
        )
}

private fun iosPopNavigationTransition(): ContentTransform {
    val animationSpec = tween<IntOffset>(durationMillis = IOS_NAVIGATION_TRANSITION_DURATION_MILLIS)

    return slideInHorizontally(
        animationSpec = animationSpec,
        initialOffsetX = { fullWidth -> -fullWidth / IOS_NAVIGATION_PARALLAX_DIVISOR },
    ) togetherWith
        slideOutHorizontally(
            animationSpec = animationSpec,
            targetOffsetX = { fullWidth -> fullWidth },
        )
}
