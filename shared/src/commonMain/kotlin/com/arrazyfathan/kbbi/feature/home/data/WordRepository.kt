package com.arrazyfathan.kbbi.feature.home.data

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.data.mapper.toDomain
import com.arrazyfathan.kbbi.feature.home.data.mapper.toCachedEntity
import com.arrazyfathan.kbbi.feature.home.data.mapper.toEntity
import com.arrazyfathan.kbbi.feature.home.data.mapper.toHistoryModels
import com.arrazyfathan.kbbi.feature.home.data.mapper.toWordResultDomain
import com.arrazyfathan.kbbi.feature.home.data.mapper.toWordEntities
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.CachedTopWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.WordLocalDataSource
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.remote.TopWordsRemoteDataSource
import com.arrazyfathan.kbbi.feature.home.data.source.remote.WordRemoteDataSource
import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TopWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslateModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordResultModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.SearchHistoryRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.TopWordsRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.TranslateRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordSearchRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
class WordRepository(
    private val remoteDataSource: WordRemoteDataSource,
    private val topWordsRemoteDataSource: TopWordsRemoteDataSource,
    private val localDataSource: WordLocalDataSource,
) : WordSearchRepository,
    BookmarkRepository,
    SearchHistoryRepository,
    TranslateRepository,
    TopWordsRepository {
    override suspend fun getMeaningOfWord(word: String): AppResult<WordResultModel, DataError> {
        val normalizedWord = word.trim()
        val remoteResult = remoteDataSource.getMeaningOfWord(normalizedWord)

        if (remoteResult is AppResult.Success) {
            val remoteWord = remoteResult.data.copy(word = remoteResult.data.word.ifBlank { normalizedWord })
            try {
                withContext(Dispatchers.Default) {
                    val existingWord =
                        localDataSource.getWord(remoteWord.word)
                            ?: localDataSource.getWord(normalizedWord)
                    localDataSource.insertWord(
                        ListWordEntity(
                            word = remoteWord.word,
                            listWords = remoteWord.entries.toWordEntities(),
                            visitorCount = remoteWord.visitorCount,
                            isSaved = existingWord?.isSaved ?: false,
                            aiGenerated = remoteWord.aiGenerated,
                        ),
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // A cache write must not turn a successful remote lookup into a failed search.
            }
            return AppResult.Success(remoteWord)
        }

        val cachedWord =
            try {
                withContext(Dispatchers.Default) { localDataSource.getWord(normalizedWord) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                null
            }
        return cachedWord?.let { AppResult.Success(it.toWordResultDomain()) } ?: remoteResult
    }

    override suspend fun bookmarkWord(
        word: String,
        result: List<WordModel>,
        visitorCount: Int?,
    ): Boolean =
        withContext(Dispatchers.Default) {
            val normalizedWord = word.trim().lowercase()
            localDataSource.insertWord(
                ListWordEntity(
                    word = normalizedWord,
                    listWords = result.toWordEntities(),
                    visitorCount = visitorCount,
                    isSaved = true,
                ),
            ) != -1L
        }

    override suspend fun addToHistory(history: HistoryModel) =
        withContext(Dispatchers.Default) {
            return@withContext localDataSource.insertHistory(history.toEntity())
        }

    override suspend fun clearHistory() =
        withContext(Dispatchers.Default) {
            localDataSource.clearHistory()
        }

    override fun getAllHistories(): Flow<List<HistoryModel>> =
        localDataSource.getAllHistories().map {
            it.toHistoryModels()
        }

    override suspend fun deleteWord(word: String): Boolean =
        withContext(Dispatchers.Default) {
            localDataSource.deleteWord(word.trim().lowercase()) > 0
        }

    override fun checkIfWordIsSaved(word: String): Flow<Boolean> = localDataSource.checkWordIsSaved(word.trim())

    override fun getBookmarks(): Flow<List<ListWordModel>> =
        localDataSource.getSavedWords().map {
            it.map { entity -> entity.toDomain() }
        }

    override suspend fun getTranslation(word: String): AppResult<TranslateModel, DataError> =
        remoteDataSource.translate(word.trim())

    override suspend fun getTopWords(limit: Int): AppResult<List<TopWordModel>, DataError> {
        val remoteResult = topWordsRemoteDataSource.getTopWords(limit)
        if (remoteResult is AppResult.Success) {
            try {
                withContext(Dispatchers.Default) {
                    localDataSource.replaceTopWords(
                        remoteResult.data.mapIndexed { index, word -> word.toCachedEntity(index) },
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // Top words are still usable when the optional offline cache cannot be updated.
            }
            return remoteResult
        }

        val cachedWords =
            try {
                withContext(Dispatchers.Default) {
                    localDataSource.getTopWords().map { it.toDomain() }.take(limit.coerceAtLeast(0))
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                emptyList()
            }
        return if (cachedWords.isNotEmpty()) AppResult.Success(cachedWords) else remoteResult
    }

    override suspend fun getCachedTopWords(limit: Int): List<TopWordModel> =
        try {
            withContext(Dispatchers.Default) {
                localDataSource.getTopWords().map { it.toDomain() }.take(limit.coerceAtLeast(0))
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            emptyList()
        }
}
