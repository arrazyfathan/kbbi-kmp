package com.arrazyfathan.kbbi.feature.home.presentation.home

import androidx.compose.runtime.Composable
import com.arrazyfathan.kbbi.core.platform.SpeechInput

// Temporary platform rollout: enable other targets after their recognition issues are resolved.
internal expect val isHomeVoiceSearchEnabled: Boolean

@Composable
internal expect fun rememberHomeSpeechInput(): SpeechInput

@Composable
internal expect fun BindHomeSpeechLifecycle(onBackground: () -> Unit)
