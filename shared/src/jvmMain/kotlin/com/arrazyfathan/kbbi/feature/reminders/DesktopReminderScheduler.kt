package com.arrazyfathan.kbbi.feature.reminders

import com.arrazyfathan.kbbi.feature.reminders.data.ReminderContentProvider
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderContent
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderScheduler
import com.arrazyfathan.kbbi.feature.settings.data.SettingsPreferenceStore
import com.arrazyfathan.kbbi.feature.settings.domain.ReminderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.awt.SystemTray
import java.awt.TrayIcon
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

/** Delivers while the desktop app runs; unavailable tray support falls back to the app's log. */
class DesktopReminderScheduler(
    private val preferences: SettingsPreferenceStore,
    private val content: ReminderContentProvider,
    private val scope: CoroutineScope,
) : ReminderScheduler {
    private val mutableFallbacks = MutableStateFlow<List<ReminderContent>>(emptyList())
    override val pendingFallbacks = mutableFallbacks.asStateFlow()
    private val tasks = ConcurrentHashMap<ReminderType, Job>()
    private val delivered = ConcurrentHashMap<ReminderType, String>()

    override suspend fun reconcile() {
        tick()
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
        tasks.remove(type)?.cancel()
        mutableFallbacks.update { it.filterNot { item -> item.type == type } }
    }

    override suspend fun tick() {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val day = now.get(Calendar.DAY_OF_YEAR)
        val minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val date = "$year-$day"
        ReminderType.entries.forEach { type ->
            val preference =
                preferences.preferences.value.reminders
                    .getValue(type)
            if (!preference.enabled || minute < preference.hour * 60 + preference.minute || delivered[type] == date) return@forEach
            val item =
                content.create(type, year, day, date, preferences.preferences.value.language)
                    ?: return@forEach
            if (deliver(item)) {
                mutableFallbacks.update { it.filterNot { pending -> pending.type == type } }
            } else {
                mutableFallbacks.update { it.filterNot { pending -> pending.type == type } + item }
            }
            delivered[type] = date
        }
    }

    override suspend fun dismiss(
        type: ReminderType,
        occurrenceDate: String,
    ) {
        mutableFallbacks.update { it.filterNot { item -> item.type == type && item.occurrenceDate == occurrenceDate } }
        delivered[type] = occurrenceDate
    }

    override suspend fun schedule(type: ReminderType) {
        cancel(type)
        val setting =
            preferences.preferences.value.reminders
                .getValue(type)
        if (!setting.enabled) return
        val now = Calendar.getInstance()
        val next =
            (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, setting.hour)
                set(Calendar.MINUTE, setting.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
        val task =
            scope.launch {
                delay((next.timeInMillis - now.timeInMillis).coerceAtLeast(0))
                while (preferences.preferences.value.reminders
                        .getValue(type)
                        .enabled
                ) {
                    val current = Calendar.getInstance()
                    val date = "${current.get(Calendar.YEAR)}-${current.get(Calendar.DAY_OF_YEAR)}"
                    if (delivered[type] != date) {
                        val lang = preferences.preferences.value.language
                        val item =
                            content.create(
                                type,
                                current.get(Calendar.YEAR),
                                current.get(Calendar.DAY_OF_YEAR),
                                date,
                                lang,
                            )
                        if (item != null && !deliver(item)) {
                            mutableFallbacks.update { it.filterNot { pending -> pending.type == type } + item }
                        }
                        delivered[type] = date
                    }
                    val nextDay = Calendar.getInstance()
                    nextDay.add(Calendar.DAY_OF_YEAR, 1)
                    nextDay.set(Calendar.HOUR_OF_DAY, setting.hour)
                    nextDay.set(Calendar.MINUTE, setting.minute)
                    nextDay.set(Calendar.SECOND, 0)
                    nextDay.set(Calendar.MILLISECOND, 0)
                    delay((nextDay.timeInMillis - System.currentTimeMillis()).coerceAtLeast(1).milliseconds)
                }
            }
        tasks[type] = task
    }

    fun close() {
        tasks.values.forEach(Job::cancel)
        tasks.clear()
    }

    private fun deliver(item: ReminderContent): Boolean {
        if (!SystemTray.isSupported()) {
            return false
        }
        return runCatching {
            val tray = SystemTray.getSystemTray()
            val icon = tray.trayIcons.firstOrNull() ?: return@runCatching false
            icon.displayMessage(item.title, item.body, TrayIcon.MessageType.INFO)
            true
        }.getOrDefault(false)
    }
}
