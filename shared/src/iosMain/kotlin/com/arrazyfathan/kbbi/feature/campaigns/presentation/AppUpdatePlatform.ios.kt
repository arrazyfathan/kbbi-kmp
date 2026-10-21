package com.arrazyfathan.kbbi.feature.campaigns.presentation

import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults
import io.ktor.http.Url

actual fun readAppUpdateCopy(releaseId: String): AppUpdateCopy? {
    if (releaseId.isBlank() || releaseId.length > 100 || releaseId.any { !it.isLetterOrDigit() && it !in "-_" }) return null
    val prefix = "kbbi.push.update.$releaseId."
    val defaults = NSUserDefaults.standardUserDefaults
    val version = defaults.stringForKey(prefix + "version") ?: return null
    val title = defaults.stringForKey(prefix + "title") ?: return null
    val body = defaults.stringForKey(prefix + "body") ?: return null
    return AppUpdateCopy(version, title, body)
}

actual fun configuredAppReleaseUrl(): String? {
    val value = NSBundle.mainBundle.objectForInfoDictionaryKey("KBBIIosReleaseURL") as? String ?: return null
    if (value.length > 2048) return null
    val url = runCatching { Url(value) }.getOrNull() ?: return null
    return value.takeIf { url.protocol.name == "https" && url.host.isNotBlank() && url.user.isNullOrEmpty() && url.password.isNullOrEmpty() }
}
