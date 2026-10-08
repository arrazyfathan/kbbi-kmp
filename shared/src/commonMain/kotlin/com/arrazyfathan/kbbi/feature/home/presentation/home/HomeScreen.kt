package com.arrazyfathan.kbbi.feature.home.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueBg
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.SpaceGroteskFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.core.presentation.ui.LocalAppLoadingController
import com.arrazyfathan.kbbi.core.presentation.ui.asStringNonComposable
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.showToast
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.button_search
import kbbi_kmp.shared.generated.resources.did_you_mean_label
import kbbi_kmp.shared.generated.resources.empty_top_words
import kbbi_kmp.shared.generated.resources.explore_title
import kbbi_kmp.shared.generated.resources.hero_home
import kbbi_kmp.shared.generated.resources.hero_image_text
import kbbi_kmp.shared.generated.resources.home_menu_subtitle
import kbbi_kmp.shared.generated.resources.home_menu_title
import kbbi_kmp.shared.generated.resources.ic_chevron_down
import kbbi_kmp.shared.generated.resources.ic_explore
import kbbi_kmp.shared.generated.resources.ic_explore_selected
import kbbi_kmp.shared.generated.resources.ic_history
import kbbi_kmp.shared.generated.resources.ic_proverb
import kbbi_kmp.shared.generated.resources.ic_search
import kbbi_kmp.shared.generated.resources.proverb_menu_subtitle
import kbbi_kmp.shared.generated.resources.proverb_menu_title
import kbbi_kmp.shared.generated.resources.retry
import kbbi_kmp.shared.generated.resources.search_word_list_hint
import kbbi_kmp.shared.generated.resources.subtitle_text
import kbbi_kmp.shared.generated.resources.top_words_label
import kbbi_kmp.shared.generated.resources.top_words_loading
import kbbi_kmp.shared.generated.resources.welcome_text
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.lazy.items as lazyColumnItems
import androidx.compose.foundation.lazy.items as lazyRowItems

private const val HOME_SEARCH_LOADING_SOURCE = "home_search"

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (ListWordModel) -> Unit,
    onNavigateToProverb: () -> Unit,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val loadingController = LocalAppLoadingController.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(HomeAction.OnStarted)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.NavigateToDetail -> {
                    onNavigateToDetail(event.word)
                }

                is HomeEvent.ShowMessage -> {
                    showToast(event.message.asStringNonComposable())
                }
            }
        }
    }

    LaunchedEffect(state.isLoading) {
        loadingController.setBlocking(HOME_SEARCH_LOADING_SOURCE, state.isLoading)
    }

    DisposableEffect(Unit) {
        onDispose {
            loadingController.setBlocking(HOME_SEARCH_LOADING_SOURCE, false)
        }
    }

    HomeContent(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToProverb = onNavigateToProverb,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    onNavigateToProverb: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }
    var showBottomSheet by remember { mutableStateOf(false) }

    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    LaunchedEffect(state.isSearchFocused) {
        if (state.isSearchFocused) searchFocusRequester.requestFocus()
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary,
                                ),
                        ),
                ).statusBarsPadding(),
    ) {
        // Hero Image at Bottom-Right
        Image(
            painter = painterResource(Res.drawable.hero_home),
            contentDescription = stringResource(Res.string.hero_image_text),
            modifier = Modifier.align(Alignment.BottomEnd).fillMaxHeight(0.35f),
            contentScale = ContentScale.FillHeight,
        )

        // Main Content Container
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Welcome Text
            Text(
                text = stringResource(Res.string.welcome_text),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 28.sp,
                fontFamily = MetropolisFontFamily,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 36.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = stringResource(Res.string.subtitle_text),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 14.sp,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search Bar Row
                    Box(
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        TextField(
                            value = state.searchQuery,
                            onValueChange = { onAction(HomeAction.OnSearchQueryChanged(it)) },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(55.dp)
                                    .focusRequester(searchFocusRequester)
                                    .onFocusChanged { onAction(HomeAction.OnSearchFocusChanged(it.isFocused)) },
                            placeholder = {
                                Text(
                                    text = stringResource(Res.string.search_word_list_hint),
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = TextP,
                                )
                            },
                            textStyle =
                                TextStyle(
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = TextH1,
                                ),
                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Search,
                                ),
                            keyboardActions =
                                KeyboardActions(
                                    onSearch = {
                                        if (state.searchQuery.isNotBlank()) {
                                            onAction(HomeAction.OnSearchSubmitted(state.searchQuery))
                                            focusManager.clearFocus()
                                        }
                                    },
                                ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors =
                                TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = TextH1,
                                    unfocusedTextColor = TextH1,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                ),
                        )

                        // Search Button (Slides In / Out)
                        this@Column.AnimatedVisibility(
                            visible = state.searchQuery.isNotBlank(),
                            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                            modifier = Modifier.align(Alignment.CenterEnd),
                        ) {
                            Surface(
                                onClick = {
                                    if (state.searchQuery.isNotBlank()) {
                                        onAction(HomeAction.OnSearchSubmitted(state.searchQuery))
                                        focusManager.clearFocus()
                                    }
                                },
                                modifier = Modifier.size(55.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondary,
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_search),
                                        contentDescription = stringResource(Res.string.button_search),
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.histories.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            lazyRowItems(state.histories.take(3), key = { it.word }) { history ->
                                Card(
                                    modifier =
                                        Modifier.clickable {
                                            onAction(HomeAction.OnSearchSubmitted(history.word))
                                        },
                                    shape = RoundedCornerShape(100.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(0.dp),
                                ) {
                                    Row(
                                        modifier =
                                            Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 6.dp,
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_history),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                            modifier = Modifier.size(14.dp),
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            history.word,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                            fontFamily = InterFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    state.searchError?.let { error ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = error.asString(),
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { onAction(HomeAction.OnRetrySearch) }) {
                                Text(stringResource(Res.string.retry), color = Color.White)
                            }
                        }
                    }

                    if (state.topWords.isNotEmpty()) {
                        Column {
                            Text(
                                text = stringResource(Res.string.top_words_label),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 14.sp,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(Modifier.height(12.dp))
                            LazyHorizontalStaggeredGrid(
                                rows = StaggeredGridCells.Fixed(2),
                                modifier = Modifier.fillMaxWidth().height(70.dp),
                                horizontalItemSpacing = 6.dp,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                items(state.topWords, key = { it.word }) { topWord ->
                                    Card(
                                        modifier =
                                            Modifier.clickable {
                                                focusManager.clearFocus()
                                                onAction(HomeAction.OnTopWordClick(topWord.word))
                                            },
                                        shape = RoundedCornerShape(32.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                        border =
                                            BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.onPrimary,
                                            ),
                                        elevation = CardDefaults.cardElevation(0.dp),
                                    ) {
                                        Row(
                                            modifier =
                                                Modifier
                                                    .defaultMinSize(minHeight = 34.dp)
                                                    .padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                "${topWord.rank}.",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                topWord.word,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (state.isTopWordsLoading) {
                        Text(stringResource(Res.string.top_words_loading), color = Color.White)
                    } else if (state.topWordsError != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                state.topWordsError.asString(),
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { onAction(HomeAction.OnRetryTopWords) }) {
                                Text(stringResource(Res.string.retry), color = Color.White)
                            }
                        }
                    } else {
                        Text(stringResource(Res.string.empty_top_words), color = Color.White)
                    }

                    if (state.topWords.isNotEmpty() && state.topWordsError != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                state.topWordsError.asString(),
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { onAction(HomeAction.OnRetryTopWords) }) {
                                Text(stringResource(Res.string.retry), color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (state.suggestions.isNotEmpty()) {
                    SearchSuggestions(
                        suggestions = state.suggestions,
                        isDidYouMean = state.suggestionMode == HomeSuggestionMode.DidYouMean,
                        onSuggestionClick = { suggestion ->
                            focusManager.clearFocus()
                            onAction(HomeAction.OnSuggestionClick(suggestion))
                        },
                        modifier = Modifier.align(Alignment.TopStart).padding(top = 63.dp),
                    )
                }
            }
        }

        Surface(
            onClick = { showBottomSheet = !showBottomSheet },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp),
            shape = RoundedCornerShape(100.dp),
            color = Color.White,
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(if (showBottomSheet) Res.drawable.ic_explore_selected else Res.drawable.ic_explore),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(Res.string.explore_title),
                    color = TextH1,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    painter = painterResource(Res.drawable.ic_chevron_down),
                    contentDescription = null,
                    tint = TextP,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        // Modal Bottom Sheet Menu
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .padding(bottom = 32.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.home_menu_title),
                        color = TextH1,
                        fontSize = 20.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(Res.string.home_menu_subtitle),
                        color = TextP,
                        fontSize = 14.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HomeMenuCard(
                            icon = Res.drawable.ic_proverb,
                            title = stringResource(Res.string.proverb_menu_title),
                            subtitle = stringResource(Res.string.proverb_menu_subtitle),
                            onClick = {
                                showBottomSheet = false
                                onNavigateToProverb()
                            },
                            modifier = Modifier.weight(1f),
                        )

                        repeat(2) {
                            HomeMenuPlaceholderCard(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun HomeContentPreview() {
    KBBITheme {
        HomeContent(
            state =
                HomeState(
                    histories =
                        listOf(
                            com.arrazyfathan.kbbi.feature.home.domain.model
                                .HistoryModel("bahasa"),
                        ),
                    topWords = listOf(TopWordUi(1, "hati"), TopWordUi(2, "kata")),
                    searchQuery = "ka",
                    suggestions = listOf("kata", "katalog"),
                ),
            onAction = {},
            onNavigateToProverb = {},
        )
    }
}

@Composable
private fun SearchSuggestions(
    suggestions: List<String>,
    isDidYouMean: Boolean,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp)) {
            if (isDidYouMean) {
                item {
                    Text(
                        text = stringResource(Res.string.did_you_mean_label),
                        color = TextP,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        modifier =
                            Modifier.padding(
                                start = 16.dp,
                                top = 16.dp,
                                end = 16.dp,
                                bottom = 8.dp,
                            ),
                    )
                }
            }
            lazyColumnItems(suggestions, key = { it }) { suggestion ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSuggestionClick(suggestion) }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_search),
                        contentDescription = null,
                        tint = TextP,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = suggestion,
                        color = TextH1,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeMenuCard(
    icon: DrawableResource,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(120.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BlueBg),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = TextH1,
                fontSize = 13.sp,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = TextP,
                fontSize = 11.sp,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                lineHeight = 14.sp,
            )
        }
    }
}

@Composable
private fun HomeMenuPlaceholderCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BlueBg),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {}
}
