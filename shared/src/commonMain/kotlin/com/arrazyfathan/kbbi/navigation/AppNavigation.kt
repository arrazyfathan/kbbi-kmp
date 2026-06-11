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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.arrazyfathan.kbbi.BindSystemBarColor
import com.arrazyfathan.kbbi.core.presentation.designsystem.components.AppLottieAnimation
import com.arrazyfathan.kbbi.core.presentation.ui.LocalAppLoadingController
import com.arrazyfathan.kbbi.core.presentation.ui.rememberAppLoadingController
import com.arrazyfathan.kbbi.feature.bookmark.presentation.navigation.bookmarksEntry
import com.arrazyfathan.kbbi.feature.detail.presentation.navigation.DetailKey
import com.arrazyfathan.kbbi.feature.detail.presentation.navigation.detailEntry
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.presentation.navigation.HomeKey
import com.arrazyfathan.kbbi.feature.home.presentation.navigation.homeEntry
import com.arrazyfathan.kbbi.feature.words.presentation.navigation.wordsEntry
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val IOS_NAVIGATION_TRANSITION_DURATION_MILLIS = 350
private const val IOS_NAVIGATION_PARALLAX_DIVISOR = 3
private const val BOTTOM_NAVIGATION_TRANSITION_DURATION_MILLIS = 220

@Composable
fun MainApp() {
    val navigationState =
        rememberNavigationState(
            startRoute = HomeKey,
            topLevelRoutes = topLevelRoutes,
        )
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val currentRoute = navigationState.currentRoute
    val isDetailVisible = currentRoute is DetailKey
    val loadingController = rememberAppLoadingController()
    val isUiBlocked by remember {
        derivedStateOf { loadingController.isBlocking }
    }

    BindSystemBarColor(isDetailVisible = isDetailVisible)
    BindBrowserNavigation(navigationState)

    val entries =
        navigationState.toDecoratedEntries(
            entryProvider {
                val navigateToDetail: (ListWordModel) -> Unit = { word ->
                    navigator.navigate(DetailKey(word))
                }
                homeEntry(onNavigateToDetail = navigateToDetail)
                wordsEntry(onNavigateToDetail = navigateToDetail)
                bookmarksEntry(onNavigateToDetail = navigateToDetail)
                detailEntry()
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
                    topLevelDestinations.forEach { destination ->
                        val isSelected = navigationState.topLevelRoute == destination.key
                        Box(
                            modifier =
                                Modifier.weight(1f).fillMaxHeight().clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = false, radius = 24.dp),
                                ) {
                                    if (!isUiBlocked && !isSelected) {
                                        navigator.navigate(destination.key)
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter =
                                    painterResource(
                                        resource = if (isSelected) destination.selectedIcon else destination.icon,
                                    ),
                                contentDescription = stringResource(destination.title),
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
