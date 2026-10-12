package com.arrazyfathan.kbbi.feature.home.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.explore_title
import kbbi_kmp.shared.generated.resources.explore_toggle_state_collapsed
import kbbi_kmp.shared.generated.resources.explore_toggle_state_expanded
import kbbi_kmp.shared.generated.resources.ic_chevron_down
import kbbi_kmp.shared.generated.resources.ic_explore
import kbbi_kmp.shared.generated.resources.ic_explore_selected
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val EXPLORE_TOGGLE_BOTTOM_OFFSET_DP = 96
private const val EXPLORE_TOGGLE_HORIZONTAL_MARGIN_DP = 16
private const val EXPLORE_TOGGLE_CHEVRON_DURATION_MILLIS = 240
private const val EXPLORE_TOGGLE_ICON_ROTATION_DURATION_MILLIS = 520
private const val EXPLORE_TOGGLE_ICON_ROTATION_DEGREES = 180f
private const val EXPLORE_TOGGLE_CONTENT_ENTER_DURATION_MILLIS = 260
private const val EXPLORE_TOGGLE_CONTENT_EXIT_DURATION_MILLIS = 200
private const val EXPLORE_TOGGLE_FLOAT_AMPLITUDE_DP = 5
private const val EXPLORE_TOGGLE_FLOAT_DURATION_MILLIS = 1500
private const val EXPLORE_TOGGLE_MOTION_BLUR_DURATION_MILLIS = 320
private const val MORPH_MOTION_BLUR_DURATION_MILLIS = 300
private const val MORPH_MOTION_BLUR_MAX_STRETCH = 0.06f
private const val MORPH_MOTION_BLUR_ALPHA_DROP = 0.08f
private const val MORPH_MOTION_BLUR_MAX_RADIUS_PX = 14f

/**
 * A short, front-loaded "velocity" pulse (0 -> 1 -> 0) that plays whenever [isActive] changes.
 * 0 means crisp at rest, 1 means peak motion. Used to drive [morphMotionBlur].
 */
@Composable
private fun rememberMorphMotionBlur(
    isActive: Boolean,
    blurDurationMillis: Int = MORPH_MOTION_BLUR_DURATION_MILLIS,
): Float {
    val motionBlur = remember { Animatable(0f) }
    // Skips the very first composition so entering the screen (e.g. re-navigating to Home) does not
    // fire a spurious blur pulse; only real toggles animate.
    var isFirstComposition by remember { mutableStateOf(true) }
    LaunchedEffect(isActive) {
        if (isFirstComposition) {
            isFirstComposition = false
            return@LaunchedEffect
        }
        motionBlur.snapTo(0f)
        motionBlur.animateTo(
            targetValue = 0f,
            animationSpec =
                keyframes {
                    durationMillis = blurDurationMillis
                    0f at 0
                    1f at blurDurationMillis / 3
                    0f at blurDurationMillis using LinearOutSlowInEasing
                },
        )
    }
    return motionBlur.value
}

/**
 * Fake directional motion blur along the horizontal axis: a horizontal stretch plus a slight
 * fade, upgraded to a real GPU [BlurEffect] on API 31+. [strength] must be 0f..1f.
 */
private fun Modifier.morphMotionBlur(
    strength: Float,
    maxStretch: Float = MORPH_MOTION_BLUR_MAX_STRETCH,
    maxRadiusPx: Float = MORPH_MOTION_BLUR_MAX_RADIUS_PX,
    alphaDrop: Float = MORPH_MOTION_BLUR_ALPHA_DROP,
): Modifier =
    graphicsLayer {
        if (strength > 0.001f) {
            scaleX = 1f + (maxStretch * strength)
            scaleY = 1f - (maxStretch * 0.5f * strength)
            alpha = 1f - (alphaDrop * strength)
            if (BlurEffect(
                    maxRadiusPx * strength,
                    maxRadiusPx * strength,
                    TileMode.Clamp,
                ).isSupported()
            ) {
                val radius = maxRadiusPx * strength
                renderEffect = BlurEffect(radius, radius, TileMode.Clamp)
            }
        } else {
            scaleX = 1f
            scaleY = 1f
            alpha = 1f
            renderEffect = null
        }
    }

@Composable
internal fun ExploreToggle(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNavigateToProverb: () -> Unit,
    onNavigateToFigure: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAiSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "exploreTogglePress",
    )
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = EXPLORE_TOGGLE_CHEVRON_DURATION_MILLIS),
        label = "exploreToggleChevron",
    )
    val exploreIconRotation by animateFloatAsState(
        targetValue = if (isExpanded) EXPLORE_TOGGLE_ICON_ROTATION_DEGREES else 0f,
        animationSpec =
            tween(
                durationMillis = EXPLORE_TOGGLE_ICON_ROTATION_DURATION_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
        label = "exploreToggleIconRotation",
    )
    val floatTransition = rememberInfiniteTransition(label = "exploreToggleFloat")
    val floatProgress by floatTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = EXPLORE_TOGGLE_FLOAT_DURATION_MILLIS,
                        easing = LinearOutSlowInEasing,
                    ),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "exploreToggleFloatProgress",
    )
    val floatFactor by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 1f,
        animationSpec = tween(durationMillis = EXPLORE_TOGGLE_CONTENT_ENTER_DURATION_MILLIS),
        label = "exploreToggleFloatFactor",
    )
    val cornerPercent by animateFloatAsState(
        targetValue = if (isExpanded) 8f else 50f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        label = "exploreToggleCorner",
    )
    val toggleStateDescription =
        stringResource(
            if (isExpanded) {
                Res.string.explore_toggle_state_expanded
            } else {
                Res.string.explore_toggle_state_collapsed
            },
        )

    val toggleShape = RoundedCornerShape(percent = cornerPercent.roundToInt())
    val toggleElevation by animateDpAsState(
        targetValue = if (isExpanded) 12.dp else 2.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        label = "exploreToggleElevation",
    )

    // Motion blur: a short pulse fired on every morph.
    // Peaks early (front-loaded velocity), then eases back to crisp at rest.
    val motionBlurStrength =
        rememberMorphMotionBlur(
            isActive = isExpanded,
            blurDurationMillis = EXPLORE_TOGGLE_MOTION_BLUR_DURATION_MILLIS,
        )

    Box(
        modifier = modifier.fillMaxWidth().morphMotionBlur(strength = motionBlurStrength),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier =
                Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y = ((floatProgress - 0.5f) * floatFactor * EXPLORE_TOGGLE_FLOAT_AMPLITUDE_DP * 2).dp.roundToPx(),
                        )
                    }.wrapContentWidth()
                    .shadow(elevation = toggleElevation, shape = toggleShape, clip = false)
                    .clip(toggleShape)
                    .background(MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.wrapContentWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = {
                        fadeIn(
                            animationSpec = tween(durationMillis = EXPLORE_TOGGLE_CONTENT_ENTER_DURATION_MILLIS),
                        ) togetherWith
                            fadeOut(
                                animationSpec = tween(durationMillis = EXPLORE_TOGGLE_CONTENT_EXIT_DURATION_MILLIS),
                            ) using
                            SizeTransform(clip = false) { _, _ ->
                                spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                )
                            }
                    },
                    contentAlignment = Alignment.BottomCenter,
                    label = "exploreToggleContent",
                ) { expanded ->
                    if (expanded) {
                        ExploreMenuContent(
                            onNavigateToProverb = onNavigateToProverb,
                            onNavigateToFigure = onNavigateToFigure,
                            onNavigateToSettings = onNavigateToSettings,
                            onNavigateToAiSettings = onNavigateToAiSettings,
                        )
                    } else {
                        Spacer(modifier = Modifier)
                    }
                }

                Row(
                    modifier =
                        Modifier
                            .clickable(
                                interactionSource = interactionSource,
                                indication = LocalIndication.current,
                                onClick = onToggle,
                            ).graphicsLayer {
                                scaleX = pressScale
                                scaleY = pressScale
                            }.defaultMinSize(minHeight = 18.dp)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .semantics {
                                stateDescription = toggleStateDescription
                            },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter =
                            painterResource(
                                resource = if (isExpanded) Res.drawable.ic_explore_selected else Res.drawable.ic_explore,
                            ),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp).rotate(exploreIconRotation),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(resource = Res.string.explore_title),
                        color = TextH1,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_chevron_down),
                        contentDescription = null,
                        tint = TextP,
                        modifier = Modifier.size(16.dp).rotate(chevronRotation),
                    )
                }

                if (isExpanded) Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
