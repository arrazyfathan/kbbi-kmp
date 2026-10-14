package com.arrazyfathan.kbbi.feature.home.presentation.home

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.arrazyfathan.kbbi.core.platform.SpeechInput
import org.koin.compose.koinInject

internal actual val isHomeVoiceSearchEnabled: Boolean = false

@Composable
internal actual fun rememberHomeSpeechInput(): SpeechInput = koinInject()

@Composable
internal actual fun BindHomeSpeechLifecycle(onBackground: () -> Unit) {
    LifecycleEventEffect(Lifecycle.Event.ON_STOP, onEvent = onBackground)
}
