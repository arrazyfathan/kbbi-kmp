package com.arrazyfathan.kbbi.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.arrazyfathan.kbbi.feature.settings.data.SettingsPreferenceStore
import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignSubscriptionReconciler
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext

class AndroidCampaignSubscriptionReconciler(
    private val context: Context,
) : CampaignSubscriptionReconciler {
    override fun requestReconcile() {
        AndroidCampaignTopics.enqueue(context)
    }
}

class AndroidCampaignTopics(
    context: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (FirebaseApp.getApps(applicationContext).isEmpty()) return@withContext Result.success()
        try {
            val settings = GlobalContext.get().get<SettingsPreferenceStore>().preferences.value
            val permissionGranted =
                Build.VERSION.SDK_INT < 33 ||
                    ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
            val enabled =
                settings.campaignNotificationsEnabled && permissionGranted &&
                    NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
            val isDebugBuild = applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
            val prefix = if (isDebugBuild) "development_" else "production_"
            listOf("word_of_day", "trending_words", "proverbs").forEach { topic ->
                val task =
                    if (enabled) FirebaseMessaging.getInstance().subscribeToTopic(prefix + topic)
                    else FirebaseMessaging.getInstance().unsubscribeFromTopic(prefix + topic)
                Tasks.await(task)
            }

            val updateEnabled =
                settings.updateNotificationsEnabled && !isDebugBuild && permissionGranted &&
                    NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
            val updateTask =
                if (updateEnabled) FirebaseMessaging.getInstance().subscribeToTopic("production_app_updates")
                else FirebaseMessaging.getInstance().unsubscribeFromTopic("production_app_updates")
            Tasks.await(updateTask)

            if (!enabled) cancelEditorialNotifications()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun cancelEditorialNotifications() {
        val preferences = applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        preferences.all.keys.forEach { key ->
            NotificationManagerCompat.from(applicationContext).cancel(key.hashCode())
        }
    }

    companion object {
        private const val PREFERENCES = "editorial_deliveries"
        private const val UNIQUE_WORK = "campaign-topic-reconcile"

        fun enqueue(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<AndroidCampaignTopics>().build(),
            )
        }
    }
}
