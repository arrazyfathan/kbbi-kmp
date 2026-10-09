package com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.presentation.designsystem.Grey
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.Red
import com.arrazyfathan.kbbi.core.presentation.designsystem.SpaceGroteskFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.core.presentation.designsystem.components.AppLottieAnimation
import com.arrazyfathan.kbbi.feature.bookmark.domain.BookmarkLayout
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.ai_definition_badge
import kbbi_kmp.shared.generated.resources.ai_definition_compact_badge
import kbbi_kmp.shared.generated.resources.app_name
import kbbi_kmp.shared.generated.resources.bookmark_grid_view
import kbbi_kmp.shared.generated.resources.bookmark_list_view
import kbbi_kmp.shared.generated.resources.bookmarks_screen_subtitle
import kbbi_kmp.shared.generated.resources.bookmarks_title
import kbbi_kmp.shared.generated.resources.cancel
import kbbi_kmp.shared.generated.resources.close
import kbbi_kmp.shared.generated.resources.delete
import kbbi_kmp.shared.generated.resources.delete_word_message
import kbbi_kmp.shared.generated.resources.delete_word_title
import kbbi_kmp.shared.generated.resources.empty_bookmarks_message
import kbbi_kmp.shared.generated.resources.hero_saved
import kbbi_kmp.shared.generated.resources.ic_delete
import kbbi_kmp.shared.generated.resources.ic_grid_view
import kbbi_kmp.shared.generated.resources.ic_list_view
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.lazy.items as listItems

@Composable
fun BookmarksScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (ListWordModel) -> Unit,
) {
    val viewModel: BookmarksViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    BookmarksScreenContent(
        state = state,
        onNavigateToDetail = onNavigateToDetail,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
private fun BookmarksScreenContent(
    state: BookmarksState,
    onNavigateToDetail: (ListWordModel) -> Unit,
    onAction: (BookmarksAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var wordToDelete by remember { mutableStateOf<ListWordModel?>(null) }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding(),
    ) {
        // Background Hero Image if not empty
        if (state.bookmarks.isNotEmpty()) {
            Image(
                painter = painterResource(Res.drawable.hero_saved),
                contentDescription = stringResource(Res.string.app_name),
                modifier = Modifier.align(Alignment.BottomEnd).fillMaxHeight(0.35f),
                contentScale = ContentScale.FillHeight,
            )
        }

        // Empty layout if empty
        if (state.bookmarks.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppLottieAnimation(
                    assetPath = "files/empty.json",
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.empty_bookmarks_message),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                )
            }
        }

        // Bookmarks List / Grid
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.bookmarks_title),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontFamily = MetropolisFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                )
                BookmarkLayoutToggle(
                    selectedLayout = state.bookmarkLayout,
                    onLayoutSelected = { onAction(BookmarksAction.OnLayoutSelected(it)) },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.bookmarks_screen_subtitle),
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = state.bookmarkLayout,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    val duration = tween<Float>(220)
                    (
                        fadeIn(duration) +
                            scaleIn(
                                duration,
                                initialScale = 0.98f,
                            )
                    ) togetherWith (fadeOut(duration) + scaleOut(duration, targetScale = 0.98f))
                },
                label = "bookmarkLayoutContent",
            ) { layout ->
                when (layout) {
                    BookmarkLayout.GRID -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp),
                        ) {
                            items(state.bookmarks, key = { it.word }) { item ->
                                BookmarkItem(
                                    model = item,
                                    onClick = { onNavigateToDetail(item) },
                                    onDeleteInitiated = { wordToDelete = item },
                                )
                            }
                        }
                    }

                    BookmarkLayout.LIST -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding =
                                PaddingValues(
                                    start = 8.dp,
                                    top = 8.dp,
                                    end = 8.dp,
                                    bottom = 80.dp,
                                ),
                        ) {
                            listItems(state.bookmarks, key = { it.word }) { item ->
                                SwipeableBookmarkListItem(
                                    model = item,
                                    onClick = { onNavigateToDetail(item) },
                                    onDeleteInitiated = { wordToDelete = item },
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        wordToDelete?.let { item ->
            DeleteConfirmationDialog(
                title = stringResource(Res.string.delete_word_title),
                message = stringResource(Res.string.delete_word_message),
                okTitle = stringResource(Res.string.delete),
                cancelTitle = stringResource(Res.string.cancel),
                onConfirm = {
                    onAction(BookmarksAction.OnDeleteConfirmed(item.word))
                    wordToDelete = null
                },
                onDismiss = {
                    wordToDelete = null
                },
            )
        }
    }
}

@Composable
private fun BookmarkLayoutToggle(
    selectedLayout: BookmarkLayout,
    onLayoutSelected: (BookmarkLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val indicatorOffset =
        animateIntOffsetAsState(
            targetValue =
                with(density) {
                    IntOffset(
                        x = if (selectedLayout == BookmarkLayout.GRID) 3.dp.roundToPx() else 43.dp.roundToPx(),
                        y = 3.dp.roundToPx(),
                    )
                },
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            label = "bookmarkLayoutIndicator",
        )
    Box(
        modifier =
            modifier
                .width(86.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White),
    ) {
        Box(
            modifier =
                Modifier
                    .offset { indicatorOffset.value }
                    .width(40.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.primary),
        )
        Row(modifier = Modifier.padding(3.dp).selectableGroup()) {
            listOf(BookmarkLayout.GRID, BookmarkLayout.LIST).forEach { layout ->
                val selected = layout == selectedLayout
                val description =
                    stringResource(
                        if (layout == BookmarkLayout.GRID) Res.string.bookmark_grid_view else Res.string.bookmark_list_view,
                    )
                val icon =
                    if (layout == BookmarkLayout.GRID) Res.drawable.ic_grid_view else Res.drawable.ic_list_view
                Box(
                    modifier =
                        Modifier
                            .width(40.dp)
                            .height(30.dp)
                            .selectable(
                                selected = selected,
                                onClick = { onLayoutSelected(layout) },
                                role = Role.RadioButton,
                            ).semantics { contentDescription = description },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = if (selected) Color.White else TextP,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableBookmarkListItem(
    model: ListWordModel,
    onClick: () -> Unit,
    onDeleteInitiated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val latestOnDelete by rememberUpdatedState(onDeleteInitiated)
    LaunchedEffect(dismissState.settledValue) {
        if (dismissState.settledValue == SwipeToDismissBoxValue.EndToStart) {
            latestOnDelete()
            dismissState.reset()
        }
    }
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Red)
                        .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_delete),
                    contentDescription = stringResource(Res.string.delete),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(88.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = model.word.replaceFirstChar { it.uppercase() },
                        color = TextH1,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (model.aiGenerated) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.09f),
                        ) {
                            Text(
                                text = stringResource(Res.string.ai_definition_compact_badge),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text =
                        model.listWords.firstOrNull()?.meanings?.firstOrNull()?.description?.takeIf(
                            String::isNotBlank,
                        ) ?: model.listWords
                            .firstOrNull()
                            ?.entry
                            .orEmpty(),
                    color = TextP,
                    fontFamily = InterFontFamily,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkItem(
    model: ListWordModel,
    onClick: () -> Unit,
    onDeleteInitiated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDeleteOverlayVisible by remember { mutableStateOf(false) }
    val aiSourceDescription = stringResource(Res.string.ai_definition_badge)

    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    val triggerVibration = {
        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
    }

    Box(
        modifier = modifier.padding(4.dp).fillMaxWidth().height(110.dp),
    ) {
        // Standard Content Card
        Card(
            modifier =
                Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).combinedClickable(
                    onClick = {
                        if (!isDeleteOverlayVisible) {
                            onClick()
                        }
                    },
                    onLongClick = {
                        triggerVibration()
                        isDeleteOverlayVisible = true
                    },
                ),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = model.word.replaceFirstChar { it.uppercase() },
                        color = TextH1,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (model.aiGenerated) {
                        Text(
                            text = stringResource(Res.string.ai_definition_compact_badge),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.09f))
                                    .semantics { contentDescription = aiSourceDescription }
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text =
                        model.listWords.firstOrNull()?.meanings?.firstOrNull()?.description?.takeIf(
                            String::isNotBlank,
                        ) ?: model.listWords
                            .firstOrNull()
                            ?.entry
                            .orEmpty(),
                    color = TextP,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // Slide-in Red Delete Overlay
        AnimatedVisibility(
            visible = isDeleteOverlayVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Card(
                modifier =
                    Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).clickable {
                        onDeleteInitiated()
                        isDeleteOverlayVisible = false
                    },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Red),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Close/Cancel overlay button on top right
                    IconButton(
                        onClick = { isDeleteOverlayVisible = false },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(24.dp),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.close),
                            contentDescription = stringResource(Res.string.cancel),
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                    }

                    // Large Trash Can in the Center
                    Icon(
                        painter = painterResource(Res.drawable.ic_delete),
                        contentDescription = stringResource(Res.string.delete),
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center).size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: String,
    okTitle: String,
    cancelTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
            ) {
                Text(
                    text = title,
                    color = TextH1,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = message,
                    color = TextH1,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Cancel button
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(64.dp))
                                .background(Grey)
                                .clickable { onDismiss() }
                                .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = cancelTitle,
                            color = TextH1,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // OK/Confirm button
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(64.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { onConfirm() }
                                .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = okTitle,
                            color = Color.White,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}
