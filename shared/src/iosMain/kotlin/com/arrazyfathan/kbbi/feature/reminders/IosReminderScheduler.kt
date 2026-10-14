package com.arrazyfathan.kbbi.feature.reminders

import com.arrazyfathan.kbbi.feature.reminders.data.ReminderContentProvider
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderDestination
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderScheduler
import com.arrazyfathan.kbbi.feature.settings.data.SettingsPreferenceStore
import com.arrazyfathan.kbbi.feature.settings.domain.ReminderType
import io.ktor.http.encodeURLPathPart
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

class IosReminderScheduler(
    private val preferences: SettingsPreferenceStore,
    private val contentProvider: ReminderContentProvider,
) : ReminderScheduler {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun reconcile() {
        val enabledTypes =
            ReminderType.entries.filter {
                preferences.preferences.value.reminders
                    .getValue(it)
                    .enabled
            }
        if (enabledTypes.isEmpty()) {
            ReminderType.entries.forEach { cancel(it) }
            return
        }
        if (!requestPermission()) {
            enabledTypes.forEach { type ->
                val preference =
                    preferences.preferences.value.reminders
                        .getValue(type)
                preferences.setReminder(type, preference.copy(enabled = false))
                cancel(type)
            }
            return
        }
        ReminderType.entries.forEach { type ->
            if (type in enabledTypes) schedule(type) else cancel(type)
        }
    }

    override suspend fun cancel(type: ReminderType) {
        center.removePendingNotificationRequestsWithIdentifiers(
            (0 until SCHEDULE_DAYS).map {
                identifier(
                    type,
                    it,
                )
            },
        )
    }

    override suspend fun schedule(type: ReminderType) {
        val preference =
            preferences.preferences.value.reminders
                .getValue(type)
        if (!preference.enabled) return
        if (!requestPermission()) {
            preferences.setReminder(type, preference.copy(enabled = false))
            cancel(type)
            return
        }
        cancel(type)
        val calendar = NSCalendar.currentCalendar
        val now = NSDate()
        val settings =
            preferences.preferences.value.reminders
                .getValue(type)
        repeat(SCHEDULE_DAYS) { offset ->
            val dateParts = NSDateComponents().apply { day = offset.toLong() }
            val date = calendar.dateByAddingComponents(dateParts, now, 0u) ?: return@repeat
            val parts =
                calendar.components(
                    NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
                    fromDate = date,
                )
            if (offset == 0 && (
                    settings.hour <
                        calendar
                            .component(
                                NSCalendarUnitHour,
                                fromDate = now,
                            ).toInt() || settings.hour ==
                        calendar
                            .component(NSCalendarUnitHour, fromDate = now)
                            .toInt() && settings.minute <=
                        calendar
                            .component(
                                NSCalendarUnitMinute,
                                fromDate = now,
                            ).toInt()
                )
            ) {
                return@repeat
            }
            val dayOfYear =
                calendar
                    .ordinalityOfUnit(
                        NSCalendarUnitDay,
                        inUnit = platform.Foundation.NSCalendarUnitYear,
                        forDate = date,
                    ).toInt()
            val dateKey = "${parts.year}-${parts.month}-${parts.day}"
            val reminder =
                contentProvider.create(
                    type,
                    parts.year.toInt(),
                    dayOfYear,
                    dateKey,
                    preferences.preferences.value.language,
                ) ?: return@repeat
            val route =
                when (val destination = reminder.destination) {
                    is ReminderDestination.Word -> "kbbi://word/${destination.word.encodeURLPathPart()}"
                    is ReminderDestination.Proverb -> "kbbi://proverb/${destination.slug.encodeURLPathPart()}"
                    ReminderDestination.Bookmarks -> "kbbi://bookmarks"
                }
            val notification =
                UNMutableNotificationContent().apply {
                    setTitle(reminder.title)
                    setBody(reminder.body)
                    setUserInfo(mapOf("url" to route, "reminder_id" to dateKey))
                }
            val triggerParts =
                calendar.components(
                    NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
                    fromDate = date,
                )
            triggerParts.hour = settings.hour.toLong()
            triggerParts.minute = settings.minute.toLong()
            val trigger =
                UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    triggerParts,
                    repeats = false,
                )
            val request =
                UNNotificationRequest.requestWithIdentifier(
                    identifier(type, offset),
                    notification,
                    trigger,
                )
            center.addNotificationRequest(request) { _ -> }
        }
    }

    private suspend fun requestPermission(): Boolean =
        suspendCancellableCoroutine { continuation ->
            center.requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound,
            ) { granted, _ ->
                if (continuation.isActive) continuation.resume(granted)
            }
        }

    private fun identifier(
        type: ReminderType,
        offset: Int,
    ) = "kbbi-reminder-${type.name.lowercase()}-$offset"

    private companion object {
        const val SCHEDULE_DAYS = 14
    }
}
