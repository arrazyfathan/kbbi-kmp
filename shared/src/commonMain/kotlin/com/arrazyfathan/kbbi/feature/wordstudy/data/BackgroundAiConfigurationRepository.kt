package com.arrazyfathan.kbbi.feature.wordstudy.data

import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.AiConfigurationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** OS credential managers perform blocking work; each caller owns its suspension. */
class BackgroundAiConfigurationRepository(
    json: Json,
) : AiConfigurationRepository {
    private val store by lazy { AiConfigurationStore(json) }
    override val credentialsAreSessionOnly get() = AiConfigurationPlatformStore.credentialsAreSessionOnly()
    override val configuration =
        flow {
            val initialized = withContext(Dispatchers.Default) { store }
            emitAll(initialized.configuration)
        }

    override suspend fun saveCustomProvider(
        providerId: String?,
        name: String,
        baseUrl: String,
        models: List<String>,
        selectedModel: String,
        apiKey: String?,
    ) = withContext(Dispatchers.Default) {
        store.saveCustomProvider(
            providerId,
            name,
            baseUrl,
            models,
            selectedModel,
            apiKey,
        )
    }

    override suspend fun selectProviderMode(mode: AiProviderMode) = withContext(Dispatchers.Default) { store.selectProviderMode(mode) }

    override suspend fun selectBackendProvider(
        providerId: String,
        model: String,
    ) = withContext(Dispatchers.Default) { store.selectBackendProvider(providerId, model) }

    override suspend fun selectCustomProvider(
        providerId: String,
        model: String,
    ) = withContext(Dispatchers.Default) { store.selectCustomProvider(providerId, model) }

    override suspend fun removeCustomProvider(providerId: String) =
        withContext(Dispatchers.Default) { store.removeCustomProvider(providerId) }

    override suspend fun getCustomCredentials(providerId: String?) =
        withContext(Dispatchers.Default) { store.getCustomCredentials(providerId) }
}
