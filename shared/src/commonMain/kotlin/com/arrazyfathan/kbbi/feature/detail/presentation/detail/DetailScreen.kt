package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueBg
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.core.presentation.ui.AppAlertState
import com.arrazyfathan.kbbi.core.presentation.ui.AppAlertType
import com.arrazyfathan.kbbi.core.presentation.ui.AppTopAlert
import com.arrazyfathan.kbbi.core.presentation.ui.UiText
import com.arrazyfathan.kbbi.core.utils.setPlainText
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.book
import kbbi_kmp.shared.generated.resources.book_solid
import kbbi_kmp.shared.generated.resources.bookmark
import kbbi_kmp.shared.generated.resources.bookmarked
import kbbi_kmp.shared.generated.resources.copy
import kbbi_kmp.shared.generated.resources.copy_success
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

private const val DETAIL_ALERT_DURATION_MILLIS = 2_200L
private val WORD_CLASS_ANNOTATION_REGEX = Regex("""\s*\[(.*?)\]\s*""")

@Composable
fun DetailScreen(
    listWordModel: ListWordModel,
    viewModel: DetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var alertState by remember { mutableStateOf<AppAlertState?>(null) }
    var alertKey by remember { mutableIntStateOf(0) }

    fun showAlert(
        message: UiText,
        type: AppAlertType = AppAlertType.Success,
    ) {
        alertState = AppAlertState(message = message, type = type)
        alertKey++
    }

    LaunchedEffect(listWordModel.word) {
        viewModel.onAction(DetailAction.OnStarted(listWordModel.word.lowercase()))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailEvent.ShowMessage -> {
                    showAlert(UiText.StringResource(event.messageRes))
                }
            }
        }
    }

    LaunchedEffect(alertKey) {
        if (alertState != null) {
            delay(DETAIL_ALERT_DURATION_MILLIS.milliseconds)
            alertState = null
        }
    }

    DetailContent(
        listWordModel = listWordModel,
        state = state,
        onAction = viewModel::onAction,
        onShowAlert = { message, type -> showAlert(message, type) },
        alertState = alertState,
    )
}

@Composable
fun DetailContent(
    listWordModel: ListWordModel,
    state: DetailState,
    onAction: (DetailAction) -> Unit,
    onShowAlert: (UiText, AppAlertType) -> Unit,
    alertState: AppAlertState?,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val bookmarkInteractionSource = remember { MutableInteractionSource() }
    val isBookmarkPressed by bookmarkInteractionSource.collectIsPressedAsState()
    val bookmarkButtonScale by animateFloatAsState(
        targetValue = if (isBookmarkPressed) 0.92f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "bookmark-button-scale",
    )
    val collapsedTitleAlpha by remember {
        derivedStateOf {
            val progress =
                if (lazyListState.firstVisibleItemIndex > 0) {
                    1f
                } else {
                    (lazyListState.firstVisibleItemScrollOffset / 300f).coerceIn(0f, 1f)
                }
            when {
                progress <= 0.7f -> 0f
                progress >= 0.8f -> 1f
                else -> (progress - 0.7f) / 0.1f
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(BlueBg),
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 132.dp),
        ) {
            // Header spacing and expanded title
            item {
                Spacer(
                    modifier = Modifier.statusBarsPadding().height(96.dp),
                )
                Text(
                    text = listWordModel.word.replaceFirstChar { it.uppercase() },
                    color = TextH1,
                    fontSize = 34.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                )
            }

            // Word details cards
            itemsIndexed(listWordModel.listWords) { index, wordModel ->
                WordEntryCard(
                    index = index,
                    wordModel = wordModel,
                    onCopyClick = {
                        var copiedText = ""
                        for ((i, item) in wordModel.meanings.withIndex()) {
                            val cleanWordClass = cleanWordClass(item.wordClass).trim()
                            val cleanDescription = cleanMeaningDescription(item.description)
                            copiedText += "${i + 1}. $cleanWordClass $cleanDescription\n\n"
                        }
                        coroutineScope.launch {
                            clipboard.setPlainText(copiedText.trim())
                            onShowAlert(
                                UiText.StringResource(Res.string.copy_success),
                                AppAlertType.Success,
                            )
                        }
                    },
                )
            }
        }

        // Top Custom Collapsed Toolbar (displays title on scroll)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .shadow(elevation = if (collapsedTitleAlpha > 0f) 8.dp else 0.dp)
                    .background(BlueBg)
                    .statusBarsPadding()
                    .height(72.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = listWordModel.word.replaceFirstChar { it.uppercase() },
                color = TextH1,
                fontSize = 26.sp,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer(alpha = collapsedTitleAlpha),
            )
        }

        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 44.dp)
                    .width(180.dp)
                    .height(54.dp)
                    .graphicsLayer {
                        scaleX = bookmarkButtonScale
                        scaleY = bookmarkButtonScale
                    }
                    .shadow(
                        elevation = if (isBookmarkPressed) 8.dp else 14.dp,
                        shape = RoundedCornerShape(100.dp),
                        clip = false,
                    )
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (state.isSaved) TextH1 else Color.White)
                    .clickable(
                        enabled = !state.isBookmarkUpdating,
                        interactionSource = bookmarkInteractionSource,
                        indication = ripple(),
                    ) {
                        onAction(
                            DetailAction.OnBookmarkClick(
                                listWordModel.word.lowercase(),
                                listWordModel.listWords,
                            ),
                        )
                    }
                    .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter =
                    painterResource(
                        resource = if (state.isSaved) Res.drawable.book_solid else Res.drawable.book,
                    ),
                contentDescription = stringResource(Res.string.bookmark),
                tint = if (state.isSaved) Color.White else TextH1,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text =
                    stringResource(
                        if (state.isSaved) Res.string.bookmarked else Res.string.bookmark,
                    ),
                color = if (state.isSaved) Color.White else TextH1,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }

        AppTopAlert(state = alertState)
    }
}

@Composable
fun WordEntryCard(
    index: Int,
    wordModel: WordModel,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Circular Badge
                Box(
                    modifier = Modifier.size(30.dp).background(TextH1, shape = RoundedCornerShape(100.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = Color.White,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = wordModel.entry,
                    color = TextH1,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Meanings list (aligned start with entry_text, i.e., 30.dp circular badge + 16.dp spacer = 46.dp)
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 46.dp),
            ) {
                wordModel.meanings.forEachIndexed { meaningIndex, meaning ->
                    val annotatedText =
                        buildMeaningText(
                            position = meaningIndex,
                            wordClass = meaning.wordClass,
                            rawDescription = meaning.description,
                        )

                    Text(
                        text = annotatedText,
                        color = TextP,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Copy Button (aligned end with meanings, height 50dp)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd,
            ) {
                Button(
                    onClick = onCopyClick,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextH1),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.height(44.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(Res.drawable.copy),
                            contentDescription = stringResource(Res.string.copy),
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.copy),
                            color = Color.White,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun buildMeaningText(
    position: Int,
    wordClass: String,
    rawDescription: String,
): AnnotatedString {
    val number = "${position + 1}. "
    val cleanWordClass = cleanWordClass(wordClass)
    val cleanDescription = " ${cleanMeaningDescription(rawDescription)}"

    return buildAnnotatedString {
        append(number)
        withStyle(
            style =
                SpanStyle(
                    color = Color(0xFF2E494C),
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
        ) {
            append(cleanWordClass)
        }
        append(cleanDescription)
    }
}

internal fun cleanWordClass(wordClass: String): String = wordClass.replace(WORD_CLASS_ANNOTATION_REGEX, " ")

internal fun cleanMeaningDescription(description: String): String = description.substringBefore('?')
