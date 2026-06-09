package com.arrazyfathan.kbbi.feature.splash.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.core.presentation.designsystem.BluePrimary
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.components.AppLottieAnimation
import com.arrazyfathan.kbbi.getAppVersionName
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.logo_desc
import kbbi_kmp.shared.generated.resources.logo_kbbi
import kbbi_kmp.shared.generated.resources.version_label
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val logoTranslationY = remember { Animatable(0f) }
    val readingAlpha = remember { Animatable(0.8f) }
    val readingTranslationY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            logoTranslationY.animateTo(
                targetValue = 100f,
                // Linear
                animationSpec = tween(durationMillis = 2000, easing = { it }),
            )
        }
        launch {
            readingAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2000, easing = { it }),
            )
        }
        launch {
            readingTranslationY.animateTo(
                targetValue = -80f,
                animationSpec = tween(durationMillis = 2000, easing = { it }),
            )
        }
        delay(3000.milliseconds)
        onTimeout()
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(BluePrimary),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, logoTranslationY.value.toInt()) }
                    .graphicsLayer {
                        translationY = 0.2f * size.height
                    },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(Res.drawable.logo_kbbi),
                contentDescription = stringResource(Res.string.logo_desc),
                modifier = Modifier.padding(bottom = 16.dp),
            )

            AppLottieAnimation(
                assetPath = "files/loading.json",
                modifier = Modifier.fillMaxWidth().height(100.dp),
            )
        }

        AppLottieAnimation(
            assetPath = "files/reading.json",
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, (readingTranslationY.value + 100).toInt()) }
                    .alpha(readingAlpha.value),
            iterations = 1,
        )

        val versionText =
            stringResource(
                Res.string.version_label,
                getAppVersionName(),
            )
        Text(
            text = versionText,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = InterFontFamily,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .safeDrawingPadding()
                    .padding(bottom = 16.dp),
        )
    }
}
