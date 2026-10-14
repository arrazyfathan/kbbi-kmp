package com.arrazyfathan.kbbi.feature.reminders.data

import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordCatalogRepository
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbModel
import com.arrazyfathan.kbbi.feature.proverb.domain.repository.ProverbRepository
import com.arrazyfathan.kbbi.feature.reminders.domain.DailyReminderSelector
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderDestination
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderTime
import com.arrazyfathan.kbbi.feature.settings.domain.AppLanguage
import com.arrazyfathan.kbbi.feature.settings.domain.ReminderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ReminderContentProviderTest {
    @Test
    fun dailyWordIsStableForTheSameDayAndSkipsBlankCatalogEntries() =
        runTest {
            val provider = provider(words = listOf("", "kata", "buku"))

            val first =
                provider.create(ReminderType.DAILY_WORD, 2026, 287, "2026-10-14", AppLanguage.ENGLISH)
            val repeated =
                provider.create(ReminderType.DAILY_WORD, 2026, 287, "2026-10-14", AppLanguage.ENGLISH)

            assertEquals(first, repeated)
            assertEquals("Word of the day", first?.title)
            assertEquals(
                "Learn “${(first?.destination as ReminderDestination.Word).word}” today.",
                first.body,
            )
            assertNull(DailyReminderSelector.select(emptyList<String>(), 2026, 287))
        }

    @Test
    fun dailyProverbUsesCachedContentAndItsSlugAsDestination() =
        runTest {
            val provider =
                provider(
                    proverbs =
                        listOf(
                            ProverbModel("", "A", "blank-text", null),
                            ProverbModel("Air beriak tanda tak dalam.", "A", "air-beriak", null),
                        ),
                )

            val reminder =
                provider.create(
                    ReminderType.DAILY_PROVERB,
                    2026,
                    287,
                    "2026-10-14",
                    AppLanguage.INDONESIAN,
                )

            assertEquals("Peribahasa harian", reminder?.title)
            assertEquals("Air beriak tanda tak dalam.", reminder?.body)
            assertEquals(ReminderDestination.Proverb("air-beriak"), reminder?.destination)
        }

    @Test
    fun bookmarkReviewUsesLocalizedCountAndHasNoContentWhenEmpty() =
        runTest {
            val provider =
                provider(
                    bookmarks =
                        listOf(
                            ListWordModel("kata", emptyList()),
                            ListWordModel("buku", emptyList()),
                        ),
                )

            val reminder =
                provider.create(
                    ReminderType.BOOKMARK_REVIEW,
                    2026,
                    287,
                    "2026-10-14",
                    AppLanguage.INDONESIAN,
                )

            assertEquals("Ada 2 kata tersimpan untuk ditinjau.", reminder?.body)
            assertEquals(ReminderDestination.Bookmarks, reminder?.destination)
            assertNull(
                provider(bookmarks = emptyList()).create(
                    ReminderType.BOOKMARK_REVIEW,
                    2026,
                    287,
                    "2026-10-14",
                    AppLanguage.ENGLISH,
                ),
            )
            assertNull(
                provider(words = emptyList()).create(
                    ReminderType.DAILY_WORD,
                    2026,
                    287,
                    "2026-10-14",
                    AppLanguage.ENGLISH,
                ),
            )
            assertNull(
                provider(proverbs = emptyList()).create(
                    ReminderType.DAILY_PROVERB,
                    2026,
                    287,
                    "2026-10-14",
                    AppLanguage.ENGLISH,
                ),
            )
        }

    @Test
    fun reminderTimeRejectsValuesOutsideTheClock() {
        assertEquals(ReminderTime(23, 59), ReminderTime(23, 59))
        assertFailsWith<IllegalArgumentException> { ReminderTime(-1, 0) }
        assertFailsWith<IllegalArgumentException> { ReminderTime(24, 0) }
        assertFailsWith<IllegalArgumentException> { ReminderTime(0, 60) }
    }

    private fun provider(
        words: List<String> = listOf("kata"),
        proverbs: List<ProverbModel> = emptyList(),
        bookmarks: List<ListWordModel> = emptyList(),
    ) = ReminderContentProvider(
        words =
            object : WordCatalogRepository {
                override suspend fun getWords() = words
            },
        proverbs =
            object : ProverbRepository {
                override fun getListProverbs(query: String): Flow<androidx.paging.PagingData<ProverbModel>> = flowOf()

                override suspend fun getCachedProverbsForReminder() = proverbs

                override suspend fun getProverbMeaning(slug: String) = error("Not used in reminder content tests")
            },
        bookmarks =
            object : BookmarkRepository {
                override suspend fun bookmarkWord(
                    word: String,
                    result: List<com.arrazyfathan.kbbi.feature.home.domain.model.WordModel>,
                    visitorCount: Int?,
                    aiGenerated: Boolean,
                ) = false

                override suspend fun deleteWord(word: String) = false

                override fun checkIfWordIsSaved(word: String): Flow<Boolean> = flowOf(false)

                override fun getBookmarks(): Flow<List<ListWordModel>> = flowOf(bookmarks)
            },
    )
}
