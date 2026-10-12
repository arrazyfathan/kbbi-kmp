package com.arrazyfathan.kbbi.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import com.arrazyfathan.kbbi.feature.figure.presentation.navigation.FigureDetailKey
import com.arrazyfathan.kbbi.feature.figure.presentation.navigation.FigureKey
import com.arrazyfathan.kbbi.feature.figure.presentation.navigation.figureDetailEntry
import com.arrazyfathan.kbbi.feature.figure.presentation.navigation.figureEntry
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.presentation.navigation.homeEntry
import com.arrazyfathan.kbbi.feature.proverb.presentation.navigation.ProverbKey
import com.arrazyfathan.kbbi.feature.proverb.presentation.navigation.proverbEntry
import com.arrazyfathan.kbbi.feature.settings.presentation.navigation.LegalDocumentKey
import com.arrazyfathan.kbbi.feature.settings.presentation.navigation.SettingsKey
import com.arrazyfathan.kbbi.feature.settings.presentation.navigation.settingsEntry
import com.arrazyfathan.kbbi.feature.words.presentation.navigation.wordsEntry
import com.arrazyfathan.kbbi.feature.wordstudy.presentation.ai.AiSettingsRoute

@Composable
internal fun MainApp(navigationState: NavigationState) {
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val currentRoute = navigationState.currentRoute
    val isDetailVisible = currentRoute is DetailKey || currentRoute is FigureDetailKey
    val showBottomNavigation = currentRoute in topLevelRoutes
    val loadingController = rememberAppLoadingController()
    val isUiBlocked by remember {
        derivedStateOf { loadingController.isBlocking }
    }

    BindSystemBarColor(isDetailVisible = isDetailVisible)

    val entries =
        navigationState.toDecoratedEntries(
            entryProvider {
                val navigateToDetail: (ListWordModel) -> Unit = { word ->
                    navigator.navigate(DetailKey(word.word))
                }
                homeEntry(
                    onNavigateToDetail = navigateToDetail,
                    onNavigateToProverb = {
                        navigator.navigate(ProverbKey())
                    },
                    onNavigateToFigure = {
                        navigator.navigate(FigureKey)
                    },
                    onNavigateToSettings = { navigator.navigate(SettingsKey) },
                    onNavigateToAiSettings = { navigator.navigate(AiSettingsKey) },
                )
                wordsEntry(onNavigateToDetail = navigateToDetail)
                bookmarksEntry(onNavigateToDetail = navigateToDetail)
                proverbEntry(
                    onNavigateBack = {
                        if (!isUiBlocked) {
                            navigator.goBack()
                        }
                    },
                )
                figureEntry(
                    onNavigateBack = { navigator.goBack() },
                    onNavigateToDetail = { slug -> navigator.navigate(FigureDetailKey(slug)) },
                )
                figureDetailEntry(onNavigateBack = { navigator.goBack() })
                settingsEntry(
                    onBack = { navigator.goBack() },
                    onOpenLegal = { document -> navigator.navigate(LegalDocumentKey(document)) },
                )
                detailEntry(
                    onNavigateBack = navigator::goBack,
                    onNavigateToAiSettings = { navigator.navigate(AiSettingsKey) },
                )
                entry<AiSettingsKey>(clazzContentKey = DestinationParser::path) {
                    AiSettingsRoute(onNavigateBack = navigator::goBack)
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
                transitionSpec = {
                    appNavigationTransition(
                        resolveAppNavigationMotion(
                            initialContentKey = initialState.entries.lastOrNull()?.contentKey,
                            targetContentKey = targetState.entries.lastOrNull()?.contentKey,
                            isPop = false,
                        ),
                    )
                },
                popTransitionSpec = {
                    appNavigationTransition(
                        resolveAppNavigationMotion(
                            initialContentKey = initialState.entries.lastOrNull()?.contentKey,
                            targetContentKey = targetState.entries.lastOrNull()?.contentKey,
                            isPop = true,
                        ),
                    )
                },
                predictivePopTransitionSpec = {
                    appNavigationTransition(
                        resolveAppNavigationMotion(
                            initialContentKey = initialState.entries.lastOrNull()?.contentKey,
                            targetContentKey = targetState.entries.lastOrNull()?.contentKey,
                            isPop = true,
                        ),
                    )
                },
            )

            AnimatedVisibility(
                visible = showBottomNavigation,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                enter =
                    slideInVertically(
                        animationSpec = tween(durationMillis = 200, delayMillis = 80),
                        initialOffsetY = { it },
                    ) + fadeIn(animationSpec = tween(durationMillis = 160, delayMillis = 80)),
                exit =
                    slideOutVertically(
                        animationSpec = tween(durationMillis = 140),
                        targetOffsetY = { it },
                    ) + fadeOut(animationSpec = tween(durationMillis = 100)),
            ) {
                BottomNavigation(
                    selectedScreen = navigationState.topLevelRoute,
                    isUiBlocked = isUiBlocked,
                    onScreenSelected = navigator::navigate,
                )
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
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .pointerInput(Unit) {
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
