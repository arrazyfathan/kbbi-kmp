@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.arrazyfathan.kbbi.feature.home.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import com.arrazyfathan.kbbi.core.platform.SpeechInput
import org.koin.compose.koinInject

internal actual val isHomeVoiceSearchEnabled: Boolean = false

@Composable
internal actual fun rememberHomeSpeechInput(): SpeechInput = koinInject()

@Composable
internal actual fun BindHomeSpeechLifecycle(onBackground: () -> Unit) {
    val latestOnBackground by rememberUpdatedState(onBackground)
    DisposableEffect(Unit) {
        val center = NSNotificationCenter.defaultCenter
        // Modal Compose layers can change controller appearance without backgrounding the app.
        val observer = center.addObserverForName(
            UIApplicationDidEnterBackgroundNotification, null, NSOperationQueue.mainQueue,
        ) { latestOnBackground() }
        onDispose { center.removeObserver(observer) }
    }
}
