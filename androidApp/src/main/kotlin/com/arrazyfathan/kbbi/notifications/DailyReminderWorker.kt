package com.arrazyfathan.kbbi.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arrazyfathan.kbbi.MainActivity
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.feature.reminders.data.ReminderContentProvider
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderDestination
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderScheduler
import com.arrazyfathan.kbbi.feature.settings.data.SettingsPreferenceStore
import com.arrazyfathan.kbbi.feature.settings.domain.ReminderType
import kotlinx.coroutines.CancellationException
import org.koin.core.context.GlobalContext
import java.util.Calendar

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val type =
            inputData
                .getString(TYPE_KEY)
                ?.let { runCatching { ReminderType.valueOf(it) }.getOrNull() }
                ?: return Result.failure()
        return try {
            val koin = GlobalContext.get()
            val preferences = koin.get<SettingsPreferenceStore>().preferences.value
            if (preferences.reminders.getValue(type).enabled && canNotify()) {
                val date = Calendar.getInstance()
                val y = date.get(Calendar.YEAR)
                val day = date.get(Calendar.DAY_OF_YEAR)
                val dateKey =
                    "$y-${date.get(Calendar.MONTH) + 1}-${date.get(Calendar.DAY_OF_MONTH)}"
                val content =
                    koin
                        .get<ReminderContentProvider>()
                        .create(type, y, day, dateKey, preferences.language)
                if (content != null && !alreadyDelivered(type, dateKey)) post(content)
            }
            koin.get<ReminderScheduler>().schedule(type)
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun canNotify(): Boolean =
        NotificationManagerCompat
            .from(applicationContext)
            .areNotificationsEnabled() && (
            Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        )

    private fun alreadyDelivered(
        type: ReminderType,
        date: String,
    ): Boolean =
        applicationContext
            .getSharedPreferences(STORE, Context.MODE_PRIVATE)
            .getString(type.name, null) == date

    private fun post(content: com.arrazyfathan.kbbi.feature.reminders.domain.ReminderContent) {
        val channel = "kbbi_reminder_${content.type.name.lowercase()}"
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channel,
                    channelName(content.type),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }
        val uri =
            when (val destination = content.destination) {
                is ReminderDestination.Word -> "kbbi://word/${Uri.encode(destination.word)}"
                is ReminderDestination.Proverb -> "kbbi://proverb/${Uri.encode(destination.slug)}"
                ReminderDestination.Bookmarks -> "kbbi://bookmarks"
            }
        val intent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(uri),
                applicationContext,
                MainActivity::class.java,
            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending =
            PendingIntent.getActivity(
                applicationContext,
                uri.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat
                .Builder(applicationContext, channel)
                .setSmallIcon(R.drawable.ic_new_icon_foreground)
                .setContentTitle(content.title)
                .setContentText(content.body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        NotificationManagerCompat
            .from(applicationContext)
            .notify(4100 + content.type.ordinal, notification)
        applicationContext.getSharedPreferences(STORE, Context.MODE_PRIVATE).edit {
            putString(content.type.name, content.occurrenceDate)
        }
    }

    private fun channelName(type: ReminderType): String =
        when (type) {
            ReminderType.DAILY_WORD -> "Word of the day"
            ReminderType.DAILY_PROVERB -> "Daily proverb"
            ReminderType.BOOKMARK_REVIEW -> "Bookmark review"
        }

    companion object {
        const val TYPE_KEY = "reminder_type"
        private const val STORE = "kbbi_reminder_delivery"
    }
}
