package com.arrazyfathan.kbbi.core.platform

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlin.coroutines.resume

class IosNotificationPermission : NotificationPermission {
    override suspend fun request(): CapabilityResult<Unit> =
        suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound,
            ) { granted, _ ->
                if (continuation.isActive) {
                    continuation.resume(if (granted) CapabilityResult.Success(Unit) else CapabilityResult.Denied)
                }
            }
        }

    override fun openSettings(): CapabilityResult<Unit> =
        runCatching {
            val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
                ?: return CapabilityResult.Failed(FailureReason.INVALID_INPUT)
            UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any?>(), completionHandler = null)
            CapabilityResult.Success(Unit)
        }.getOrElse { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) }
}
