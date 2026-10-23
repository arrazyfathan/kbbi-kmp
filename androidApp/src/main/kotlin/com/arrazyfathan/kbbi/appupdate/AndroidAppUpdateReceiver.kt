package com.arrazyfathan.kbbi.appupdate

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.feature.appupdate.AndroidAppUpdateLauncher
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.Executors

class AndroidAppUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        val preferences = context.getSharedPreferences(AndroidAppUpdateLauncher.PREFERENCES, Context.MODE_PRIVATE)
        if (completedId < 0L || completedId != preferences.getLong(AndroidAppUpdateLauncher.DOWNLOAD_ID, -1L)) return

        val pendingResult = goAsync()
        ioExecutor.execute {
            try {
                val manager = context.getSystemService<DownloadManager>() ?: return@execute
                val query = DownloadManager.Query().setFilterById(completedId)
                manager.query(query)?.use { cursor ->
                    if (!cursor.moveToFirst() ||
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) !=
                        DownloadManager.STATUS_SUCCESSFUL
                    ) {
                        return@execute
                    }
                } ?: return@execute
                val sourceUri = manager.getUriForDownloadedFile(completedId) ?: return@execute
                val version = preferences.getString(AndroidAppUpdateLauncher.EXPECTED_VERSION, null) ?: return@execute
                val apkFile = copyVerifiedUpdate(context, sourceUri, version) ?: return@execute
                postReadyNotification(context, apkFile, version, completedId)
            } catch (_: Exception) {
                // DownloadManager keeps its completion notification available if validation fails.
            } finally {
                preferences
                    .edit()
                    .remove(AndroidAppUpdateLauncher.DOWNLOAD_ID)
                    .remove(AndroidAppUpdateLauncher.EXPECTED_VERSION)
                    .apply()
                pendingResult.finish()
            }
        }
    }

    private fun copyVerifiedUpdate(
        context: Context,
        source: Uri,
        expectedVersion: String,
    ): File? {
        val directory = File(context.cacheDir, UPDATE_DIRECTORY).apply { mkdirs() }
        val apk = File(directory, "kbbi-update.apk")
        val temporary = File(directory, "kbbi-update.part")
        val input = context.contentResolver.openInputStream(source) ?: return null
        input.use { sourceStream ->
            FileOutputStream(temporary).use { destination ->
                val buffer = ByteArray(BUFFER_SIZE)
                var totalBytes = 0L
                while (true) {
                    val count = sourceStream.read(buffer)
                    if (count < 0) break
                    totalBytes += count
                    if (totalBytes > MAX_APK_BYTES) return null
                    destination.write(buffer, 0, count)
                }
            }
        }

        val candidate = packageInfoFromArchive(context.packageManager, temporary.path) ?: return null
        if (candidate.packageName != context.packageName || candidate.versionName != expectedVersion) return null
        val installed = packageInfoForInstalledApp(context.packageManager, context.packageName)
        if (candidate.longVersionCode() <= installed.longVersionCode()) return null
        if (candidate.signerDigests() != installed.signerDigests()) return null
        apk.delete()
        if (!temporary.renameTo(apk)) return null
        return apk
    }

    private fun packageInfoFromArchive(
        manager: PackageManager,
        path: String,
    ): PackageInfo? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manager.getPackageArchiveInfo(path, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            manager.getPackageArchiveInfo(path, PackageManager.GET_SIGNATURES)
        }

    private fun packageInfoForInstalledApp(
        manager: PackageManager,
        packageName: String,
    ): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            manager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        }

    @Suppress("DEPRECATION")
    private fun PackageInfo.signerDigests(): Set<String> {
        val signatures =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                signingInfo?.apkContentsSigners.orEmpty()
            } else {
                signatures.orEmpty()
            }
        return signatures
            .map { signature ->
                MessageDigest
                    .getInstance("SHA-256")
                    .digest(signature.toByteArray())
                    .joinToString("") { byte -> "%02x".format(byte) }
                    .lowercase()
            }.toSet()
    }

    @Suppress("DEPRECATION")
    private fun PackageInfo.longVersionCode(): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) longVersionCode else versionCode.toLong()

    private fun postReadyNotification(
        context: Context,
        apk: File,
        version: String,
        notificationId: Long,
    ) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    UPDATE_CHANNEL,
                    context.getString(R.string.app_update_notification_channel),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }
        val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.update-files", apk)
        val installIntent =
            Intent(context, AndroidAppUpdateInstallActivity::class.java)
                .putExtra(EXTRA_APK_URI, apkUri.toString())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                notificationId.toInt(),
                installIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat
                .Builder(context, UPDATE_CHANNEL)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(context.getString(R.string.app_update_ready_title, version))
                .setContentText(context.getString(R.string.app_update_ready_body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        if (Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            runCatching { NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification) }
        }
    }

    companion object {
        internal const val EXTRA_APK_URI = "apk_uri"
        private const val UPDATE_DIRECTORY = "updates"
        private const val UPDATE_CHANNEL = "app_updates"
        private const val NOTIFICATION_ID = 2601
        private const val BUFFER_SIZE = 16 * 1024
        private const val MAX_APK_BYTES = 300L * 1024L * 1024L
        private val ioExecutor = Executors.newSingleThreadExecutor()
    }
}

class AndroidAppUpdateInstallActivity : Activity() {
    private var apkUri: Uri? = null

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        apkUri = intent.getStringExtra(AndroidAppUpdateReceiver.EXTRA_APK_URI)?.toUri()
        continueInstallation()
    }

    @Deprecated("Deprecated in Android", ReplaceWith("onActivityResult(requestCode, resultCode, data)"))
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_UNKNOWN_SOURCES) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.canRequestPackageInstalls()) {
            installApk()
        } else {
            Toast.makeText(this, R.string.app_update_install_permission_error, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun continueInstallation() {
        if (apkUri == null) return finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            val settingsIntent =
                Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:$packageName"),
                )
            runCatching { startActivityForResult(settingsIntent, REQUEST_UNKNOWN_SOURCES) }.onFailure {
                Toast.makeText(this, R.string.app_update_install_permission_error, Toast.LENGTH_LONG).show()
                finish()
            }
            return
        }
        installApk()
    }

    private fun installApk() {
        val uri = apkUri ?: return finish()
        val installIntent =
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, AndroidAppUpdateLauncher.APK_MIME_TYPE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        runCatching { startActivity(installIntent) }.onFailure {
            Toast.makeText(this, R.string.app_update_install_error, Toast.LENGTH_LONG).show()
        }
        finish()
    }

    private companion object {
        const val REQUEST_UNKNOWN_SOURCES = 2602
    }
}
