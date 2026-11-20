package com.arrazyfathan.kbbi.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.arrazyfathan.kbbi.MainActivity
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignDestination
import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignEnvironment
import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignMessage
import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignPayloadParser
import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignPlatform
import com.arrazyfathan.kbbi.feature.campaigns.domain.EditorialCampaign
import com.arrazyfathan.kbbi.feature.settings.data.SettingsPreferenceStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.util.concurrent.TimeUnit
import kotlin.time.Instant

class AndroidCampaignMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    @Deprecated(
        "Firebase Messaging invokes this callback when the device token changes.",
        ReplaceWith("AndroidCampaignTopics.enqueue(this)"),
    )
    override fun onNewToken(token: String) {
        AndroidCampaignTopics.enqueue(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Notification payloads can be auto-rendered by FCM while the app is backgrounded,
        // bypassing the local category preference. The campaign sender must use data-only payloads.
        if (message.notification != null) return
        if (message.data.size > MAX_FIELDS ||
            message.data.entries.any { (key, value) ->
                key.length > MAX_FIELD_NAME_LENGTH || value.length > MAX_FIELD_VALUE_LENGTH
            }
        ) {
            return
        }
        val environment =
            if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                CampaignEnvironment.DEVELOPMENT
            } else {
                CampaignEnvironment.PRODUCTION
            }
        when (
            val parsed =
                CampaignPayloadParser.parse(
                    message.data,
                    Instant.fromEpochMilliseconds(System.currentTimeMillis()),
                    environment,
                    CampaignPlatform.ANDROID,
                )
        ) {
            is CampaignMessage.Editorial -> serviceScope.launch { deliverEditorial(parsed.value) }

            // Plan 25 owns the update screen/release policy. Do not surface an update push to a dead route.
            is CampaignMessage.AppUpdate, null -> Unit
        }
    }

    @SuppressLint("MissingPermission") // Permission is checked before posting and SecurityException is handled.
    private suspend fun deliverEditorial(campaign: EditorialCampaign) {
        val store = GlobalContext.get().get<SettingsPreferenceStore>()
        val enabled = store.preferences.first().campaignNotificationsEnabled
        if (!enabled || !canDeliver(campaign)) return

        val deliveryState = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        pruneExpired(deliveryState)
        if (deliveryState.contains(campaign.deliveryId)) return

        val destination =
            when (val item = campaign.destination) {
                is CampaignDestination.Word -> "kbbi://word/${encodePathPart(item.term)}"
                is CampaignDestination.Proverb -> "kbbi://proverb/${encodePathPart(item.slug)}"
            }
        val intent =
            Intent(this, MainActivity::class.java).apply {
                action = "com.arrazyfathan.kbbi.CAMPAIGN.${campaign.deliveryId}"
                putExtra("url", destination)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                campaign.deliveryId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    EDITORIAL_CHANNEL,
                    getString(R.string.campaign_notification_channel),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }
        val notification =
            NotificationCompat
                .Builder(this, EDITORIAL_CHANNEL)
                .setSmallIcon(R.drawable.ic_new_icon_foreground)
                .setContentTitle(campaign.title)
                .setContentText(campaign.body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(campaign.body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        if (canDeliver(campaign)) {
            try {
                NotificationManagerCompat
                    .from(this)
                    .notify(campaign.deliveryId.hashCode(), notification)
                deliveryState.edit { putLong(campaign.deliveryId, System.currentTimeMillis()) }
            } catch (_: SecurityException) {
                // OS permission can change between the final check and notify().
            }
        }
    }

    private fun canDeliver(campaign: EditorialCampaign): Boolean =
        campaign.expiresAt >= Instant.fromEpochMilliseconds(System.currentTimeMillis()) && hasPermission()

    private fun pruneExpired(preferences: android.content.SharedPreferences) {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(DELIVERY_RETENTION_DAYS)
        preferences.edit {
            preferences.all
                .filterValues { it is Long && it < cutoff }
                .keys
                .forEach(::remove)
        }
    }

    private fun hasPermission(): Boolean =
        (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
        ) &&
            NotificationManagerCompat
                .from(this)
                .areNotificationsEnabled()

    private fun encodePathPart(value: String): String =
        java.net.URLEncoder
            .encode(value, Charsets.UTF_8.name())
            .replace("+", "%20")

    companion object {
        private const val MAX_FIELDS = 24
        private const val MAX_FIELD_NAME_LENGTH = 64
        private const val MAX_FIELD_VALUE_LENGTH = 1024
        private const val DELIVERY_RETENTION_DAYS = 7L
        private const val PREFERENCES = "editorial_deliveries"
        private const val EDITORIAL_CHANNEL = "editorial_notifications"
    }
}
