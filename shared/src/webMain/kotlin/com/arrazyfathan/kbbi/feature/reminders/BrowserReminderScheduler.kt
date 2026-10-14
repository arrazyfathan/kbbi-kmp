package com.arrazyfathan.kbbi.feature.reminders

import com.arrazyfathan.kbbi.feature.reminders.data.ReminderContentProvider
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderContent
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderScheduler
import com.arrazyfathan.kbbi.feature.settings.data.SettingsPreferenceStore
import com.arrazyfathan.kbbi.feature.settings.domain.ReminderType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import web.notifications.Notification
import web.notifications.requestPermission
import web.storage.localStorage

external class Date {
    constructor()

    fun getFullYear(): Int

    fun getMonth(): Int

    fun getDate(): Int

    fun getHours(): Int

    fun getMinutes(): Int

    companion object {
        fun UTC(
            year: Int,
            month: Int,
            day: Int,
        ): Double
    }
}

/** Posts browser notifications while the app is open; the app lifecycle supplies the minute tick. */
class BrowserReminderScheduler(
    private val preferences: SettingsPreferenceStore,
    private val contentProvider: ReminderContentProvider,
) : ReminderScheduler {
    private val mutableFallbacks = MutableStateFlow<List<ReminderContent>>(emptyList())
    override val pendingFallbacks = mutableFallbacks.asStateFlow()

    override suspend fun reconcile() = tick()

    override suspend fun cancel(type: ReminderType) {
        mutableFallbacks.update { it.filterNot { item -> item.type == type } }
    }

    override suspend fun schedule(type: ReminderType) {
        if (notificationsSupported() && Notification.permission.toString() == "default") {
            try {
                Notification.requestPermission()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // The shared in-app fallback remains available when the browser rejects the request.
            }
        }
        if (!preferences.preferences.value.reminders
                .getValue(type)
                .enabled
        ) {
            cancel(type)
        } else {
            tick()
        }
    }

    override suspend fun tick() {
        val canNotify = notificationsSupported() && Notification.permission.toString() == "granted"
        val now = Date()
        val year = now.getFullYear()
        val month = now.getMonth()
        val day = now.getDate()
        val dayOfYear =
            ((Date.UTC(year, month, day) - Date.UTC(year, 0, 0)) / MILLIS_PER_DAY).toInt()
        val date =
            "$year-${(month + 1).toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
        val minutesNow = now.getHours() * 60 + now.getMinutes()
        ReminderType.entries.forEach { type ->
            val preference =
                preferences.preferences.value.reminders
                    .getValue(type)
            if (!preference.enabled || minutesNow < preference.hour * 60 + preference.minute) return@forEach
            if (localStorage.getItem(deliveryKey(type)) == date) return@forEach
            val reminder =
                contentProvider.create(
                    type,
                    year,
                    dayOfYear,
                    date,
                    preferences.preferences.value.language,
                ) ?: return@forEach
            if (canNotify) {
                deliver(reminder)
                localStorage.setItem(deliveryKey(type), date)
                mutableFallbacks.update { current -> current.filterNot { it.type == type } }
            } else {
                mutableFallbacks.update { current -> current.filterNot { it.type == type } + reminder }
            }
        }
    }

    override suspend fun dismiss(
        type: ReminderType,
        occurrenceDate: String,
    ) {
        localStorage.setItem(deliveryKey(type), occurrenceDate)
        mutableFallbacks.update { it.filterNot { item -> item.type == type && item.occurrenceDate == occurrenceDate } }
    }

    private fun deliver(reminder: ReminderContent) {
        Notification("${reminder.title}: ${reminder.body}")
    }

    private fun notificationsSupported(): Boolean =
        runCatching {
            Notification.permission
            true
        }.getOrDefault(false)

    private fun deliveryKey(type: ReminderType) = "kbbi.reminders.delivered.${type.name}"

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000.0
    }
}
