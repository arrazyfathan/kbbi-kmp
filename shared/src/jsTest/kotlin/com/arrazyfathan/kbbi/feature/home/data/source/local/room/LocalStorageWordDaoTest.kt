package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.WordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalStorageWordDaoTest {
    @Test
    fun unbookmarkKeepsDefinitionAvailableForOfflineLookup() = runTest {
        val dao = LocalStorageWordDao(Json, FakeWordStorage())
        val cached = ListWordEntity("air", listOf(WordEntity("air", emptyList())), isSaved = true)
        dao.insertWord(cached)

        assertEquals(1, dao.deleteWord(" AIR "))
        assertTrue(dao.getSavedWords().first().isEmpty())
        assertEquals(cached.copy(isSaved = false), dao.getWord("air"))
    }

    @Test
    fun corruptedDataIsIgnoredAndReported() = runTest {
        val storage = FakeWordStorage().apply { values["kbbi.bookmarks.v1"] = "{bad json" }
        val dao = LocalStorageWordDao(Json, storage)

        assertTrue(dao.getAllWords().first().isEmpty())
        val issue = dao.getStorageIssues().first().single()
        assertEquals("kbbi.bookmarks.v1", issue.key)
        assertEquals(StorageOperation.Read, issue.operation)
    }

    @Test
    fun quotaFailureDoesNotMutateMemoryAndIsReported() = runTest {
        val storage = FakeWordStorage().apply { failWrites = true }
        val dao = LocalStorageWordDao(Json, storage)

        assertEquals(-1L, dao.insertWord(ListWordEntity("air", emptyList())))
        assertTrue(dao.getAllWords().first().isEmpty())
        val issue = dao.getStorageIssues().first().single()
        assertEquals("kbbi.bookmarks.v1", issue.key)
        assertEquals(StorageOperation.Write, issue.operation)
        assertFalse(dao.getSavedWords().first().any { it.word == "air" })
    }

    private class FakeWordStorage : WordStorage {
        val values = mutableMapOf<String, String>()
        var failWrites = false

        override fun getItem(key: String): String? = values[key]

        override fun setItem(key: String, value: String) {
            check(!failWrites) { "Storage quota exceeded" }
            values[key] = value
        }
    }
}
