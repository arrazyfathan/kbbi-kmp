package com.arrazyfathan.kbbi.notifications

import com.arrazyfathan.kbbi.MainActivity
import com.arrazyfathan.kbbi.core.platform.CapabilityResult
import com.arrazyfathan.kbbi.core.platform.NotificationPermission
import android.content.Intent
import android.provider.Settings
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidCampaignPermission : NotificationPermission {
    override suspend fun request(): CapabilityResult<Unit> = suspendCancellableCoroutine { continuation ->
        val activity = MainActivity.current
        if (activity == null) {
            continuation.resume(CapabilityResult.Unavailable)
        } else {
            activity.requestNotificationPermission { granted ->
                if (continuation.isActive) {
                    continuation.resume(if (granted) CapabilityResult.Success(Unit) else CapabilityResult.Denied)
                }
            }
        }
    }

    override fun openSettings(): CapabilityResult<Unit> =
        runCatching {
            val activity = MainActivity.current ?: return CapabilityResult.Unavailable
            activity.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName),
            )
            CapabilityResult.Success(Unit)
        }.getOrElse { CapabilityResult.Failed(com.arrazyfathan.kbbi.core.platform.FailureReason.PLATFORM_ERROR) }
}
