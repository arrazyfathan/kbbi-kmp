package com.arrazyfathan.kbbi.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val BOTTOM_NAVIGATION_HEIGHT_DP = 80

@Composable
internal fun BottomNavigation(
    selectedScreen: NavKey?,
    isUiBlocked: Boolean,
    onScreenSelected: (NavKey) -> Unit,
) {
    val screens = topLevelDestinations
    val selectedIndex =
        selectedScreen?.let({ key -> screens.indexOfFirst { it.key == key } })?.coerceAtLeast(0)
            ?: 0

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(BOTTOM_NAVIGATION_HEIGHT_DP.dp)
                .shadow(elevation = 50.dp)
                .background(MaterialTheme.colorScheme.surface),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val itemWidth = maxWidth / screens.size
            val density = LocalDensity.current
            val selectedOffset =
                animateIntOffsetAsState(
                    targetValue =
                        with(density) {
                            IntOffset(
                                x = (itemWidth * selectedIndex).roundToPx(),
                                y = 0,
                            )
                        },
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                    label = "bottomNavigationIndicator",
                )

            Box(
                modifier =
                    Modifier
                        .offset { selectedOffset.value }
                        .width(itemWidth)
                        .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxSize().background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                                            Color.Transparent,
                                        ),
                                ),
                        ),
                )
                Box(
                    modifier =
                        Modifier.fillMaxWidth().height(4.dp).background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(50),
                        ),
                )
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                screens.forEach { screen ->
                    val isSelected = screen.key == selectedScreen
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val iconScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.88f else 1f,
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                        label = "bottomNavigationIconPress",
                    )
                    Box(
                        modifier =
                            Modifier.weight(1f).fillMaxHeight().clickable(
                                interactionSource = interactionSource,
                                indication = null,
                            ) {
                                if (!isUiBlocked && !isSelected) {
                                    onScreenSelected(screen.key)
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                modifier =
                                    Modifier
                                        .size(if (isSelected) 24.dp else 22.dp)
                                        .graphicsLayer {
                                            scaleX = iconScale
                                            scaleY = iconScale
                                        },
                                painter =
                                    painterResource(
                                        resource = if (isSelected) screen.selectedIcon else screen.icon,
                                    ),
                                contentDescription = stringResource(screen.title),
                                tint =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        Color.Gray
                                    },
                            )
                            Text(
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                                text = stringResource(screen.title),
                                color =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        Color.Gray
                                    },
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontSize = if (isSelected) 13.sp else 12.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}
