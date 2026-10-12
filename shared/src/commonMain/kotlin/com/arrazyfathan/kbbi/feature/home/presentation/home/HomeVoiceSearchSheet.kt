package com.arrazyfathan.kbbi.feature.home.presentation.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.button_search
import kbbi_kmp.shared.generated.resources.button_voice_search
import kbbi_kmp.shared.generated.resources.cancel
import kbbi_kmp.shared.generated.resources.ic_microphone
import kbbi_kmp.shared.generated.resources.ic_search
import kbbi_kmp.shared.generated.resources.voice_search_detected_label
import kbbi_kmp.shared.generated.resources.voice_search_listening
import kbbi_kmp.shared.generated.resources.voice_search_sheet_subtitle
import kbbi_kmp.shared.generated.resources.voice_search_sheet_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun VoiceSearchBottomSheetContent(
    partialText: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VoiceListeningAnimation()

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(Res.string.voice_search_sheet_title),
            color = TextH1,
            fontSize = 20.sp,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.voice_search_sheet_subtitle),
            color = TextP,
            fontSize = 14.sp,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.voice_search_detected_label),
                    color = TextP,
                    fontSize = 12.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = partialText.ifBlank { stringResource(Res.string.voice_search_listening) },
                    color = if (partialText.isBlank()) TextP else TextH1,
                    fontSize = 16.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = if (partialText.isBlank()) FontWeight.Normal else FontWeight.Medium,
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.secondary,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.cancel),
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 14.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun VoiceListeningAnimation(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "voice_listening")
    val outerPulseScale by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.18f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1100),
                repeatMode = RepeatMode.Restart,
            ),
        label = "voice_outer_pulse_scale",
    )
    val outerPulseAlpha by transition.animateFloat(
        initialValue = 0.34f,
        targetValue = 0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1100),
                repeatMode = RepeatMode.Restart,
            ),
        label = "voice_outer_pulse_alpha",
    )
    val innerPulseScale by transition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.08f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 860),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "voice_inner_pulse_scale",
    )

    Box(
        modifier = modifier.size(132.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(108.dp)
                    .graphicsLayer {
                        scaleX = outerPulseScale
                        scaleY = outerPulseScale
                        alpha = outerPulseAlpha
                    }.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(999.dp)),
        )

        Box(
            modifier =
                Modifier
                    .size(92.dp)
                    .graphicsLayer {
                        scaleX = innerPulseScale
                        scaleY = innerPulseScale
                    }.background(MaterialTheme.colorScheme.background, RoundedCornerShape(999.dp)),
        )

        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_microphone),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}

@Composable
internal fun HomeSearchVoiceButton(
    query: String,
    onSearch: () -> Unit,
    onVoiceSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    voiceSearchEnabled: Boolean = true,
) {
    val hasQuery = query.isNotBlank()
    val searchWordContentDescription = stringResource(Res.string.button_search)
    val voiceSearchContentDescription = stringResource(Res.string.button_voice_search)

    val iconMorph by animateFloatAsState(
        targetValue = if (hasQuery || !voiceSearchEnabled) 1f else 0f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        label = "searchVoiceButtonIconMorph",
    )

    Surface(
        onClick = {
            if (hasQuery) {
                onSearch()
            } else if (voiceSearchEnabled) {
                onVoiceSearchClick()
            }
        },
        enabled = hasQuery || voiceSearchEnabled,
        modifier =
            modifier.size(55.dp).semantics {
                contentDescription =
                    if (hasQuery || !voiceSearchEnabled) {
                        searchWordContentDescription
                    } else {
                        voiceSearchContentDescription
                    }
            },
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
    ) {
        Box(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            val wheelOffsetPx = with(LocalDensity.current) { 28.dp.toPx() }
            if (voiceSearchEnabled) {
                Icon(
                    painter = painterResource(Res.drawable.ic_microphone),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier.size(24.dp).graphicsLayer {
                            translationY = -wheelOffsetPx * iconMorph
                            alpha = (1f - iconMorph).coerceIn(0f, 1f)
                        },
                )
            }
            Icon(
                painter = painterResource(Res.drawable.ic_search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier.size(18.dp).graphicsLayer {
                        translationY = wheelOffsetPx * (1f - iconMorph)
                        alpha = iconMorph.coerceIn(0f, 1f)
                    },
            )
        }
    }
}
