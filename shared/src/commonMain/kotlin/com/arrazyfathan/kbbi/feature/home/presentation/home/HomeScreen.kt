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
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueBg
import com.arrazyfathan.kbbi.core.presentation.designsystem.BluePrimary
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueSecondary
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.SpaceGroteskFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.core.presentation.designsystem.components.AppLottieAnimation
import com.arrazyfathan.kbbi.core.presentation.ui.LocalAppLoadingController
import com.arrazyfathan.kbbi.core.presentation.ui.asStringNonComposable
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.showToast
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.button_search
import kbbi_kmp.shared.generated.resources.hero_home
import kbbi_kmp.shared.generated.resources.hero_image_text
import kbbi_kmp.shared.generated.resources.history_label
import kbbi_kmp.shared.generated.resources.ic_history
import kbbi_kmp.shared.generated.resources.ic_search
import kbbi_kmp.shared.generated.resources.search_word_list_hint
import kbbi_kmp.shared.generated.resources.subtitle_text
import kbbi_kmp.shared.generated.resources.swipe_label
import kbbi_kmp.shared.generated.resources.welcome_text
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val HOME_SEARCH_LOADING_SOURCE = "home_search"

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (ListWordModel) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val loadingController = LocalAppLoadingController.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.onAction(HomeAction.OnStarted)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.NavigateToDetail -> {
                    searchQuery = ""
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
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var showBottomSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(BluePrimary)
                .statusBarsPadding()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitFirstDown(requireUnconsumed = false)
                            var totalDragY = 0f
                            var isSwipeDetected = false
                            do {
                                val event = awaitPointerEvent()
                                val dragChange = event.changes.firstOrNull()
                                if (dragChange != null && dragChange.pressed) {
                                    val deltaY = dragChange.position.y - dragChange.previousPosition.y
                                    totalDragY += deltaY
                                    if (totalDragY < -150f) { // Swipe up threshold
                                        isSwipeDetected = true
                                        dragChange.consume()
                                    }
                                }
                            } while (event.changes.any { it.pressed } && !isSwipeDetected)

                            if (isSwipeDetected) {
                                showBottomSheet = true
                            }
                        }
                    }
                },
    ) {
        // Hero Image at Bottom-Right
        Image(
            painter = painterResource(Res.drawable.hero_home),
            contentDescription = stringResource(Res.string.hero_image_text),
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxHeight(0.35f),
            contentScale = ContentScale.FillHeight,
        )

        // Main Content Container
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Welcome Text
            Text(
                text = stringResource(Res.string.welcome_text),
                color = Color.White,
                fontSize = 28.sp,
                fontFamily = MetropolisFontFamily,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 36.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = stringResource(Res.string.subtitle_text),
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar Row
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { text ->
                        val filteredText = text.replace(" ", "")
                        onSearchQueryChange(filteredText)
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(55.dp),
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
                                if (searchQuery.isNotBlank()) {
                                    onAction(HomeAction.OnSearchSubmitted(searchQuery))
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
                            cursorColor = BluePrimary,
                        ),
                )

                // Search Button (Slides In / Out)
                this@Column.AnimatedVisibility(
                    visible = searchQuery.length > 2,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Surface(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                onAction(HomeAction.OnSearchSubmitted(searchQuery))
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier.size(55.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = BlueSecondary,
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

            // History Section
            if (state.histories.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.history_label),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyHorizontalStaggeredGrid(
                    rows = StaggeredGridCells.Fixed(2),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(84.dp),
                    horizontalItemSpacing = 10.dp,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.histories.take(5), key = { it.word }) { history ->
                        Card(
                            modifier =
                                Modifier.clickable {
                                    onAction(HomeAction.OnSearchSubmitted(history.word))
                                },
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(1.dp, Color.White),
                            elevation = CardDefaults.cardElevation(0.dp),
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .defaultMinSize(minHeight = 34.dp)
                                        .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_history),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = history.word,
                                    color = Color.White,
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Swipe Up Prompter at the bottom
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .clickable {
                        showBottomSheet = true
                    }.padding(bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppLottieAnimation(
                assetPath = "files/swipeblue.json",
                contentDescription = null,
                modifier = Modifier.size(50.dp),
            )

            Text(
                text = stringResource(Res.string.swipe_label),
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
            )
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
                        text = "Menu",
                        color = TextH1,
                        fontSize = 20.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Temukan fitur lainnya (Comming Soon)",
                        color = TextP,
                        fontSize = 14.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Three placeholder cards side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        repeat(3) {
                            Card(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(120.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = BlueBg),
                                elevation = CardDefaults.cardElevation(0.dp),
                            ) {}
                        }
                    }
                }
            }
        }
    }
}
