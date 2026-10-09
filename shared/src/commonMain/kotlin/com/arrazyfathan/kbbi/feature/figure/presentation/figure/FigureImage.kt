package com.arrazyfathan.kbbi.feature.figure.presentation.figure

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory

internal const val FIGURE_IMAGE_USER_AGENT =
    "KBBI Android/1.0 (https://github.com/arrazyfathan/kbbi)"

@Composable
internal fun rememberFigureImageLoader(): ImageLoader {
    val context = LocalPlatformContext.current
    val imageLoader =
        remember(context) {
            ImageLoader.Builder(context).components { add(KtorNetworkFetcherFactory()) }.build()
        }
    DisposableEffect(imageLoader) {
        onDispose(imageLoader::shutdown)
    }
    return imageLoader
}
