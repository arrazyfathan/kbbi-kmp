package com.arrazyfathan.kbbi.feature.reminders.domain

import com.arrazyfathan.kbbi.feature.settings.domain.ReminderType
import com.arrazyfathan.kbbi.widgetcore.WidgetDailyItemSelector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ReminderTime(
    val hour: Int,
    val minute: Int,
) {
    init {
        require(hour in 0..23) { "hour must be between 0 and 23" }
        require(minute in 0..59) { "minute must be between 0 and 59" }
    }
}

sealed interface ReminderDestination {
    data class Word(
        val word: String,
    ) : ReminderDestination

    data class Proverb(
        val slug: String,
    ) : ReminderDestination

    data object Bookmarks : ReminderDestination
}

data class ReminderContent(
    val type: ReminderType,
    val occurrenceDate: String,
    val title: String,
    val body: String,
    val destination: ReminderDestination,
)

/** Matches the Android source selector. Input ordering is intentionally preserved. */
object DailyReminderSelector {
    fun <T> select(
        items: List<T>,
        year: Int,
        dayOfYear: Int,
    ): T? = WidgetDailyItemSelector.select(items, year, dayOfYear)
}

private val emptyReminderFallbacks: StateFlow<List<ReminderContent>> = MutableStateFlow(emptyList())

interface ReminderScheduler {
    val pendingFallbacks: StateFlow<List<ReminderContent>>
        get() = emptyReminderFallbacks

    suspend fun reconcile()

    suspend fun cancel(type: ReminderType)

    suspend fun schedule(type: ReminderType)

    suspend fun tick() = Unit

    suspend fun dismiss(
        type: ReminderType,
        occurrenceDate: String,
    ) = Unit
}

object UnsupportedReminderScheduler : ReminderScheduler {
    override suspend fun reconcile() = Unit

    override suspend fun cancel(type: ReminderType) = Unit

    override suspend fun schedule(type: ReminderType) = Unit
}

interface ReminderPermissionGateway {
    suspend fun request(): Boolean

    fun openSettings(): Boolean
}
