package com.arrazyfathan.kbbi.feature.appupdate

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.arrazyfathan.kbbi.core.platform.CapabilityResult
import com.arrazyfathan.kbbi.core.platform.FailureReason
import com.arrazyfathan.kbbi.core.platform.UrlOpener
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateLauncher

class AndroidAppUpdateLauncher(
    private val context: Context,
    private val urlOpener: UrlOpener,
) : AppUpdateLauncher {
    override fun launch(update: AppUpdate): CapabilityResult<Unit> {
        val rawUrl = update.actionUrl ?: return CapabilityResult.Unavailable
        val uri = runCatching { rawUrl.toUri() }.getOrNull() ?: return CapabilityResult.Failed(FailureReason.INVALID_INPUT)
        if (uri.scheme != "https" || uri.encodedUserInfo != null) return CapabilityResult.Failed(FailureReason.INVALID_INPUT)

        if (!uri.lastPathSegment.orEmpty().endsWith(".apk", ignoreCase = true)) {
            return if (uri.host == GITHUB_HOST) urlOpener.open(rawUrl) else CapabilityResult.Failed(FailureReason.INVALID_INPUT)
        }
        if (uri.host != GITHUB_HOST && uri.host?.endsWith(GITHUB_USER_CONTENT_SUFFIX) != true) {
            return CapabilityResult.Failed(FailureReason.INVALID_INPUT)
        }

        return runCatching {
            val downloadManager = context.getSystemService<DownloadManager>() ?: return CapabilityResult.Unavailable
            val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            preferences.getLong(DOWNLOAD_ID, -1L).takeIf { it >= 0L }?.let(downloadManager::remove)
            val request =
                DownloadManager.Request(uri)
                    .setTitle("KBBI update")
                    .setDescription("Version ${update.latestVersion}")
                    .setMimeType(APK_MIME_TYPE)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "kbbi-update-${update.latestVersion}.apk")
            val id = downloadManager.enqueue(request)
            preferences.edit()
                .putLong(DOWNLOAD_ID, id)
                .putString(EXPECTED_VERSION, update.latestVersion)
                .apply()
            CapabilityResult.Success(Unit)
        }.getOrElse { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) }
    }

    companion object {
        const val PREFERENCES = "app_update_download"
        const val DOWNLOAD_ID = "download_id"
        const val EXPECTED_VERSION = "expected_version"
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        private const val GITHUB_HOST = "github.com"
        private const val GITHUB_USER_CONTENT_SUFFIX = ".githubusercontent.com"
    }
}
