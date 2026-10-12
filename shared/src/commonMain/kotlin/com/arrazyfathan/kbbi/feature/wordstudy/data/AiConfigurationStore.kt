package com.arrazyfathan.kbbi.feature.wordstudy.data

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiConfigurationModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomCredentialsModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomProviderModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.AiConfigurationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.random.Random

internal expect object AiConfigurationPlatformStore {
    fun isSecretStoreAvailable(): Boolean

    fun read(key: String): String?

    fun write(
        key: String,
        value: String?,
    )

    fun readSecret(key: String): String?

    fun writeSecret(
        key: String,
        value: String?,
    ): Boolean
}

class AiConfigurationStore(
    private val json: Json,
) : AiConfigurationRepository {
    private val mutex = Mutex()
    private val mutableConfiguration = MutableStateFlow(loadConfiguration())
    override val configuration = mutableConfiguration.asStateFlow()

    override suspend fun saveCustomProvider(
        providerId: String?,
        name: String,
        baseUrl: String,
        models: List<String>,
        selectedModel: String,
        apiKey: String?,
    ): AppResult<String, DataError> =
        mutex.withLock {
            if (!AiConfigurationPlatformStore.isSecretStoreAvailable()) {
                return@withLock AppResult.Error(
                    DataError.ServiceUnavailable,
                )
            }
            val current = mutableConfiguration.value
            val id =
                providerId ?: Random
                    .nextBytes(16)
                    .joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
            if (providerId != null && current.customProviders.none { it.id == id }) {
                return@withLock AppResult.Error(
                    DataError.NotFound,
                )
            }
            val normalizedName = name.trim()
            val normalizedUrl = baseUrl.trim().trimEnd('/')
            val normalizedModels = models.map(String::trim).filter(String::isNotBlank).distinct()
            if (normalizedName.isBlank() || normalizedUrl.isBlank() || normalizedModels.isEmpty() || selectedModel !in normalizedModels ||
                normalizedModels.size > 20 ||
                normalizedModels.any { it.length > 200 }
            ) {
                return@withLock AppResult.Error(DataError.BadRequest)
            }
            val secret =
                apiKey?.trim()?.takeIf(String::isNotBlank)
                    ?: AiConfigurationPlatformStore.readSecret(id)
            if (secret.isNullOrBlank()) return@withLock AppResult.Error(DataError.BadRequest)
            if (!AiConfigurationPlatformStore.writeSecret(id, secret)) {
                return@withLock AppResult.Error(
                    DataError.ServiceUnavailable,
                )
            }
            val provider =
                AiCustomProviderModel(
                    id,
                    normalizedName,
                    normalizedUrl,
                    normalizedModels,
                    selectedModel,
                    true,
                )
            val providers = current.customProviders.filterNot { it.id == id } + provider
            save(
                current.copy(
                    customProviders = providers,
                    selectedCustomProviderId = current.selectedCustomProviderId ?: id,
                ),
            )
            AppResult.Success(id)
        }

    override suspend fun selectProviderMode(mode: AiProviderMode): AppResult<Unit, DataError> =
        mutex.withLock {
            if (mode == AiProviderMode.CUSTOM && getCustomCredentials() !is AppResult.Success) {
                return@withLock AppResult.Error(
                    DataError.BadRequest,
                )
            }
            save(mutableConfiguration.value.copy(providerMode = mode))
            AppResult.Success(Unit)
        }

    override suspend fun selectBackendProvider(
        providerId: String,
        model: String,
    ): AppResult<Unit, DataError> =
        mutex.withLock {
            if (providerId.isBlank() || model.isBlank()) return@withLock AppResult.Error(DataError.BadRequest)
            save(
                mutableConfiguration.value.copy(
                    backendProviderId = providerId.trim(),
                    backendModel = model.trim(),
                ),
            )
            AppResult.Success(Unit)
        }

    override suspend fun selectCustomProvider(
        providerId: String,
        model: String,
    ): AppResult<Unit, DataError> =
        mutex.withLock {
            val current = mutableConfiguration.value
            val provider = current.customProviders.firstOrNull { it.id == providerId }
            if (provider == null || model !in provider.models ||
                AiConfigurationPlatformStore
                    .readSecret(
                        providerId,
                    ).isNullOrBlank()
            ) {
                return@withLock AppResult.Error(DataError.BadRequest)
            }
            save(
                current.copy(
                    customProviders =
                        current.customProviders.map {
                            if (it.id == providerId) {
                                it.copy(
                                    selectedModel = model,
                                )
                            } else {
                                it
                            }
                        },
                    selectedCustomProviderId = providerId,
                ),
            )
            AppResult.Success(Unit)
        }

    override suspend fun removeCustomProvider(providerId: String): AppResult<Unit, DataError> =
        mutex.withLock {
            val current = mutableConfiguration.value
            if (current.customProviders.none { it.id == providerId }) {
                return@withLock AppResult.Error(
                    DataError.NotFound,
                )
            }
            if (!AiConfigurationPlatformStore.writeSecret(
                    providerId,
                    null,
                )
            ) {
                return@withLock AppResult.Error(DataError.ServiceUnavailable)
            }
            val providers = current.customProviders.filterNot { it.id == providerId }
            val next =
                current.copy(
                    customProviders = providers,
                    selectedCustomProviderId =
                        current.selectedCustomProviderId.takeUnless { it == providerId }
                            ?: providers.firstOrNull()?.id,
                    providerMode =
                        if (providers.isEmpty() &&
                            current.providerMode == AiProviderMode.CUSTOM
                        ) {
                            AiProviderMode.BACKEND
                        } else {
                            current.providerMode
                        },
                )
            save(next)
            AppResult.Success(Unit)
        }

    override suspend fun getCustomCredentials(providerId: String?): AppResult<AiCustomCredentialsModel, DataError> {
        val current = mutableConfiguration.value
        val provider =
            current.customProviders.firstOrNull {
                it.id == (providerId ?: current.selectedCustomProviderId)
            } ?: return AppResult.Error(DataError.BadRequest)
        val key =
            AiConfigurationPlatformStore.readSecret(provider.id)?.takeIf(String::isNotBlank)
                ?: return AppResult.Error(DataError.BadRequest)
        return AppResult.Success(
            AiCustomCredentialsModel(
                provider.id,
                provider.name,
                provider.baseUrl,
                provider.selectedModel,
                key,
            ),
        )
    }

    private fun loadConfiguration(): AiConfigurationModel =
        runCatching {
            val stored =
                AiConfigurationPlatformStore
                    .read(CONFIG_KEY)
                    ?.let { json.decodeFromString<StoredConfiguration>(it) }
                    ?: return@runCatching AiConfigurationModel()
            AiConfigurationModel(
                providerMode =
                    runCatching { AiProviderMode.valueOf(stored.providerMode) }.getOrDefault(
                        AiProviderMode.BACKEND,
                    ),
                backendProviderId = stored.backendProviderId,
                backendModel = stored.backendModel,
                customProviders =
                    stored.customProviders.map {
                        it.toDomain(
                            it.id.let { id ->
                                !AiConfigurationPlatformStore.readSecret(id).isNullOrBlank()
                            },
                        )
                    },
                selectedCustomProviderId = stored.selectedCustomProviderId,
                revision = stored.revision,
            )
        }.getOrDefault(AiConfigurationModel())

    private fun save(value: AiConfigurationModel) {
        val updated = value.copy(revision = value.revision + 1)
        AiConfigurationPlatformStore.write(CONFIG_KEY, json.encodeToString(updated.toStored()))
        mutableConfiguration.value = updated
    }
}

@Serializable
private data class StoredConfiguration(
    val providerMode: String = AiProviderMode.BACKEND.name,
    val backendProviderId: String? = null,
    val backendModel: String? = null,
    val customProviders: List<StoredProvider> = emptyList(),
    val selectedCustomProviderId: String? = null,
    val revision: Long = 0,
)

@Serializable
private data class StoredProvider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val models: List<String>,
    val selectedModel: String,
)

private fun StoredProvider.toDomain(hasKey: Boolean) = AiCustomProviderModel(id, name, baseUrl, models, selectedModel, hasKey)

private fun AiConfigurationModel.toStored() =
    StoredConfiguration(
        providerMode.name,
        backendProviderId,
        backendModel,
        customProviders.map { StoredProvider(it.id, it.name, it.baseUrl, it.models, it.selectedModel) },
        selectedCustomProviderId,
        revision,
    )

private const val CONFIG_KEY = "ai.configuration.v1"
