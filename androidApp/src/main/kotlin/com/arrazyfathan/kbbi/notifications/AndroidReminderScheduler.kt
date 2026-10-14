package com.arrazyfathan.kbbi.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.arrazyfathan.kbbi.MainActivity
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderScheduler
import com.arrazyfathan.kbbi.feature.settings.data.SettingsPreferenceStore
import com.arrazyfathan.kbbi.feature.settings.domain.ReminderType
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AndroidReminderScheduler(
    private val context: Context,
    private val preferences: SettingsPreferenceStore,
) : ReminderScheduler {
    override suspend fun reconcile() {
        ReminderType.entries.forEach { type ->
            if (preferences.preferences.value.reminders
                    .getValue(type)
                    .enabled
            ) {
                schedule(type)
            } else {
                cancel(
                    type,
                )
            }
        }
    }

    override suspend fun cancel(type: ReminderType) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(type))
    }

    override suspend fun schedule(type: ReminderType) {
        val reminder =
            preferences.preferences.value.reminders
                .getValue(type)
        if (!reminder.enabled) return cancel(type)
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            MainActivity.current?.requestReminderPermission(type)
            return
        }
        val request =
            OneTimeWorkRequestBuilder<DailyReminderWorker>()
                .setInitialDelay(
                    delayUntil(
                        reminder.hour,
                        reminder.minute,
                    ),
                    TimeUnit.MILLISECONDS,
                ).setInputData(Data.Builder().putString(DailyReminderWorker.TYPE_KEY, type.name).build())
                .build()
        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(uniqueName(type), ExistingWorkPolicy.REPLACE, request)
    }

    private fun uniqueName(type: ReminderType) = "kbbi_reminder_${type.name.lowercase()}"

    private fun delayUntil(
        hour: Int,
        minute: Int,
    ): Long {
        val now = Calendar.getInstance()
        val next =
            (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
        return next.timeInMillis - now.timeInMillis
    }
}
