package com.arrazyfathan.kbbi.core.platform

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.appContext
import java.util.Locale

internal class AndroidTextShare : TextShare {
    override fun share(text: String): CapabilityResult<Unit> =
        runCatching {
            val sendIntent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
            val chooser = Intent.createChooser(sendIntent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (sendIntent.resolveActivity(appContext.packageManager) == null) {
                CapabilityResult.Unavailable
            } else {
                appContext.startActivity(chooser)
                CapabilityResult.Success(Unit)
            }
        }.getOrElse { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) }
}

internal class AndroidUrlOpener : UrlOpener {
    override fun open(url: String): CapabilityResult<Unit> {
        val uri = Uri.parse(url)
        if (uri.scheme !in setOf("http", "https") || uri.host.isNullOrBlank()) {
            return CapabilityResult.Failed(FailureReason.INVALID_INPUT)
        }

        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(appContext.packageManager) == null) {
                CapabilityResult.Unavailable
            } else {
                appContext.startActivity(intent)
                CapabilityResult.Success(Unit)
            }
        }.getOrElse { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) }
    }
}

internal class AndroidLocaleProvider : LocaleProvider {
    override fun currentLocaleTag(): String = Locale.getDefault().toLanguageTag()
}

internal class AndroidHaptics : Haptics {
    override fun perform(feedback: HapticFeedback): CapabilityResult<Unit> =
        runCatching {
            val vibrator =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    appContext.getSystemService(Vibrator::class.java)
                } ?: return CapabilityResult.Unavailable

            val durationMillis =
                when (feedback) {
                    HapticFeedback.TAP -> 12L
                    HapticFeedback.CONFIRM -> 24L
                    HapticFeedback.ERROR -> 36L
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMillis)
            }
            CapabilityResult.Success(Unit)
        }.getOrElse { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) }
}
