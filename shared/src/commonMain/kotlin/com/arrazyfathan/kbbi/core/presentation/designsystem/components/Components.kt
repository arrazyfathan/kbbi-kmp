package com.arrazyfathan.kbbi.core.presentation.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kbbi_kmp.shared.generated.resources.Res

@Composable
fun AppLottieAnimation(
    assetPath: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    iterations: Int = Compottie.IterateForever,
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes(assetPath).decodeToString(),
        )
    }

    Image(
        painter =
            rememberLottiePainter(
                composition = composition,
                iterations = iterations,
            ),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
