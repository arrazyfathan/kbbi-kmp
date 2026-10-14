package com.arrazyfathan.kbbi.feature.reminders.data

import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordCatalogRepository
import com.arrazyfathan.kbbi.feature.proverb.domain.repository.ProverbRepository
import com.arrazyfathan.kbbi.feature.reminders.domain.DailyReminderSelector
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderContent
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderDestination
import com.arrazyfathan.kbbi.feature.settings.domain.AppLanguage
import com.arrazyfathan.kbbi.feature.settings.domain.ReminderType
import kotlinx.coroutines.flow.first

class ReminderContentProvider(
    private val words: WordCatalogRepository,
    private val proverbs: ProverbRepository,
    private val bookmarks: BookmarkRepository,
) {
    suspend fun create(
        type: ReminderType,
        year: Int,
        dayOfYear: Int,
        occurrenceDate: String,
        language: AppLanguage,
    ): ReminderContent? =
        when (type) {
            ReminderType.DAILY_WORD -> {
                val word =
                    DailyReminderSelector.select(
                        words.getWords().filter(String::isNotBlank),
                        year,
                        dayOfYear,
                    ) ?: return null
                ReminderContent(
                    type,
                    occurrenceDate,
                    if (language == AppLanguage.INDONESIAN) "Kata Hari Ini" else "Word of the day",
                    if (language == AppLanguage.INDONESIAN) "Pelajari “$word” hari ini." else "Learn “$word” today.",
                    ReminderDestination.Word(word),
                )
            }

            ReminderType.DAILY_PROVERB -> {
                val proverb =
                    proverbs
                        .getCachedProverbsForReminder()
                        .filter { it.text.isNotBlank() && it.slug.isNotBlank() }
                        .let { DailyReminderSelector.select(it, year, dayOfYear) } ?: return null
                ReminderContent(
                    type,
                    occurrenceDate,
                    if (language == AppLanguage.INDONESIAN) "Peribahasa harian" else "Daily proverb",
                    proverb.text,
                    ReminderDestination.Proverb(proverb.slug),
                )
            }

            ReminderType.BOOKMARK_REVIEW -> {
                val count = bookmarks.getBookmarks().first().size
                if (count == 0) return null
                ReminderContent(
                    type,
                    occurrenceDate,
                    if (language == AppLanguage.INDONESIAN) "Tinjau kata tersimpan" else "Review your bookmarks",
                    if (language == AppLanguage.INDONESIAN) {
                        if (count == 1) "Ada 1 kata tersimpan untuk ditinjau." else "Ada $count kata tersimpan untuk ditinjau."
                    } else {
                        if (count == 1) "You have 1 saved word to review." else "You have $count saved words to review."
                    },
                    ReminderDestination.Bookmarks,
                )
            }
        }
}
