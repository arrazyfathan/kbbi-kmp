package com.arrazyfathan.kbbi.feature.home.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.arrazyfathan.kbbi.core.platform.CapabilityResult
import com.arrazyfathan.kbbi.core.platform.SpeechInput
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.compose.koinInject
import kotlin.coroutines.resume

internal actual val isHomeVoiceSearchEnabled: Boolean = true

private class MicrophonePermissionRequest {
    var pending: CancellableContinuation<Boolean>? = null
}

@Composable
internal actual fun rememberHomeSpeechInput(): SpeechInput {
    val context = LocalContext.current
    val delegate: SpeechInput = koinInject()
    val permission = remember { MicrophonePermissionRequest() }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            permission.pending?.takeIf { it.isActive }?.resume(granted)
            permission.pending = null
        }
    DisposableEffect(permission) {
        onDispose {
            permission.pending?.cancel()
            permission.pending = null
        }
    }
    return remember(delegate, context, launcher) {
        object : SpeechInput {
            override suspend fun recognize(): CapabilityResult<String> = delegate.recognize()

            override suspend fun recognizeCandidates(
                onReady: () -> Unit,
                onPartial: (List<String>) -> Unit,
            ): CapabilityResult<List<String>> {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) return CapabilityResult.Unavailable
                val granted =
                    context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ||
                        suspendCancellableCoroutine { continuation ->
                            permission.pending = continuation
                            launcher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                return if (granted) {
                    delegate.recognizeCandidates(
                        onReady,
                        onPartial,
                    )
                } else {
                    CapabilityResult.Denied
                }
            }
        }
    }
}

@Composable
internal actual fun BindHomeSpeechLifecycle(onBackground: () -> Unit) {
    LifecycleEventEffect(Lifecycle.Event.ON_STOP, onEvent = onBackground)
}
