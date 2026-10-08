package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import androidx.room.Room
import androidx.paging.PagingSource
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdProvider
import com.arrazyfathan.kbbi.feature.home.data.WordRepository
import com.arrazyfathan.kbbi.feature.home.data.source.local.WordLocalDataSource
import com.arrazyfathan.kbbi.feature.home.data.source.remote.TopWordsRemoteDataSource
import com.arrazyfathan.kbbi.feature.home.data.source.remote.WordRemoteDataSource
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.WordEntity
import com.arrazyfathan.kbbi.feature.proverb.data.NetworkProverbRepository
import com.arrazyfathan.kbbi.feature.proverb.data.ProverbPagingSource
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.ProverbLocalDataSource
import com.arrazyfathan.kbbi.feature.proverb.data.source.remote.ProverbRemoteDataSource
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbEntity
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbDetailEntity
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.room.RoomProverbDaoAdapter
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbDetailModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PersistenceContractTest {
    private val database =
        Room.inMemoryDatabaseBuilder<WordDatabase> {
            WordDatabaseConstructor.initialize()
        }.setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).build()

    @AfterTest
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun unbookmarkKeepsCachedDefinitionAndHidesWordFromSavedFlow() = runTest {
        val dao = RoomWordDaoAdapter(database.wordDao())
        val saved = ListWordEntity(
            word = "air",
            listWords = listOf(WordEntity("air", emptyList())),
            isSaved = true,
        )
        dao.insertWord(saved)
        dao.insertWord(saved.copy(visitorCount = 9))

        assertTrue(dao.checkWordIsSaved(" air ").first())
        assertEquals(1, dao.deleteWord(" AIR "))

        assertFalse(dao.checkWordIsSaved("air").first())
        assertTrue(dao.getSavedWords().first().isEmpty())
        assertEquals(saved.copy(visitorCount = 9, isSaved = false), dao.getWord("air"))
    }

    @Test
    fun historyIsTrimmedInStableNewestFirstOrderAndCanClearWithoutDeletingSavedWords() = runTest {
        val dao = RoomWordDaoAdapter(database.wordDao())
        dao.insertWord(ListWordEntity("api", emptyList(), isSaved = true))
        dao.insertHistoryAndTrim(HistoryEntity("lama", searchedAt = 1), limit = 2)
        dao.insertHistoryAndTrim(HistoryEntity("baru", searchedAt = 3), limit = 2)
        dao.insertHistoryAndTrim(HistoryEntity("tengah", searchedAt = 2), limit = 2)

        assertEquals(listOf("baru", "tengah"), dao.getListHistory().first().map { it.word })
        dao.clearHistory()

        assertTrue(dao.getListHistory().first().isEmpty())
        assertEquals("api", dao.getWord("api")?.word)
        assertTrue(dao.checkWordIsSaved("api").first())
    }

    @Test
    fun proverbPagesReplaceRefreshAndKeepStablePositions() = runTest {
        val dao = RoomProverbDaoAdapter(database.proverbDao())
        val page1 = listOf(proverb(page = 1, position = 1, slug = "b"), proverb(page = 1, position = 0, slug = "a"))
        dao.replaceProverbPage(query = "", page = 1, proverbs = page1)
        dao.replaceProverbPage(query = "", page = 2, proverbs = listOf(proverb(page = 2, position = 0, slug = "c")))

        assertEquals(listOf("a", "b"), dao.getProverbs("", 1).map { it.slug })
        dao.replaceProverbPage(query = "", page = 1, proverbs = listOf(proverb(page = 1, position = 0, slug = "fresh")))
        assertEquals(listOf("fresh"), dao.getProverbs("", 1).map { it.slug })
        assertTrue(dao.getProverbs("", 2).isEmpty())
    }

    @Test
    fun cachedProverbPageAndDetailRemainReadableWhenNetworkIsUnavailable() = runTest {
        val local = ProverbLocalDataSource(RoomProverbDaoAdapter(database.proverbDao()))
        local.replaceProverbPage("", 1, listOf(proverb(page = 1, position = 0, slug = "offline")))
        local.upsertProverbDetail(
            CachedProverbDetailEntity(
                slug = "offline",
                text = "Offline proverb",
                letter = "O",
                sourceUrl = null,
                meaning = "Cached meaning",
            ),
        )
        val remote = ProverbRemoteDataSource(
            HttpClient(MockEngine { respond("offline", status = HttpStatusCode.ServiceUnavailable) }),
        )
        val repository = NetworkProverbRepository(remote, local)

        val page = ProverbPagingSource(remote, local, query = "", pageSize = 20)
            .load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        assertEquals(listOf("offline"), (page as PagingSource.LoadResult.Page<Int, ProverbModel>).data.map { it.slug })
        assertEquals(
            AppResult.Success(ProverbDetailModel("Offline proverb", "O", "offline", null, "Cached meaning")),
            repository.getProverbMeaning("offline"),
        )
    }

    @Test
    fun cachedWordRemainsAvailableWhenNetworkIsUnavailable() = runTest {
        val local = WordLocalDataSource(RoomWordDaoAdapter(database.wordDao()))
        local.insertWord(ListWordEntity("air", listOf(WordEntity("air", emptyList()))))
        val remoteClient = HttpClient(MockEngine { respond("offline", status = HttpStatusCode.ServiceUnavailable) })
        val repository =
            WordRepository(
                remoteDataSource = WordRemoteDataSource(remoteClient, object : VisitorIdProvider {
                    override fun getVisitorId() = "test-visitor"
                }),
                topWordsRemoteDataSource = TopWordsRemoteDataSource(remoteClient),
                localDataSource = local,
            )

        assertEquals("air", (repository.getMeaningOfWord(" air ") as AppResult.Success).data.word)
    }

    private fun proverb(page: Int, position: Int, slug: String) =
        CachedProverbEntity(
            query = "",
            page = page,
            position = position,
            totalPages = 2,
            hasNextPage = page == 1,
            text = slug,
            letter = "a",
            slug = slug,
            sourceUrl = null,
        )
}
