package com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiBackendProviderCatalogModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomCredentialsModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyDefinitionModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyEntryModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudySourceModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.AiConfigurationRepository
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.WordStudyRepository
import io.ktor.http.Url

class GetAiProvidersUseCase(
    private val repository: WordStudyRepository,
) {
    suspend operator fun invoke(): AppResult<AiBackendProviderCatalogModel, DataError> = repository.getBackendProviders()
}

class SaveAiConfigurationUseCase(
    private val repository: AiConfigurationRepository,
) {
    suspend operator fun invoke(
        providerId: String?,
        name: String,
        baseUrl: String,
        models: List<String>,
        selectedModel: String,
        apiKey: String?,
    ): AppResult<String, DataError> {
        val url = normalizeAiBaseUrl(baseUrl) ?: return AppResult.Error(DataError.BadRequest)
        val normalizedModels = models.map(String::trim).filter(String::isNotBlank).distinct()
        val selected = selectedModel.trim()
        if (normalizedModels.isEmpty() || normalizedModels.size > 20 || normalizedModels.any { it.length > 200 } ||
            selected !in normalizedModels ||
            apiKey?.isBlank() == true
        ) {
            return AppResult.Error(DataError.BadRequest)
        }
        val resolvedName = name.trim().ifBlank { providerDomainName(url) }
        if (resolvedName.isBlank() || resolvedName.length > 80) return AppResult.Error(DataError.BadRequest)
        return repository.saveCustomProvider(
            providerId,
            resolvedName,
            url,
            normalizedModels,
            selected,
            apiKey?.trim(),
        )
    }
}

class SelectAiProviderModeUseCase(
    private val repository: AiConfigurationRepository,
) {
    suspend operator fun invoke(mode: AiProviderMode) = repository.selectProviderMode(mode)
}

class SelectBackendAiProviderUseCase(
    private val repository: AiConfigurationRepository,
) {
    suspend operator fun invoke(
        providerId: String,
        model: String,
    ) = repository.selectBackendProvider(providerId, model)
}

class SelectCustomAiProviderUseCase(
    private val repository: AiConfigurationRepository,
) {
    suspend operator fun invoke(
        providerId: String,
        model: String,
    ) = repository.selectCustomProvider(providerId, model)
}

class RemoveAiConfigurationUseCase(
    private val repository: AiConfigurationRepository,
) {
    suspend operator fun invoke(providerId: String) = repository.removeCustomProvider(providerId)
}

class TestAiConnectionUseCase(
    private val wordStudyRepository: WordStudyRepository,
    private val configurationRepository: AiConfigurationRepository,
) {
    suspend operator fun invoke(
        providerId: String?,
        name: String,
        baseUrl: String,
        model: String,
        apiKey: String,
    ): AppResult<Unit, DataError> {
        val normalizedUrl =
            normalizeAiBaseUrl(baseUrl) ?: return AppResult.Error(DataError.BadRequest)
        if (model.isBlank()) return AppResult.Error(DataError.BadRequest)
        val resolvedKey =
            apiKey.trim().ifBlank {
                (configurationRepository.getCustomCredentials(providerId) as? AppResult.Success)?.data?.apiKey
                    ?: return AppResult.Error(DataError.BadRequest)
            }
        val source =
            WordStudySourceModel(
                "bahasa",
                listOf(
                    WordStudyEntryModel(
                        "bahasa",
                        listOf(WordStudyDefinitionModel("n", "sistem lambang bunyi")),
                    ),
                ),
            )
        val request =
            source.toWordStudyRequest("id") ?: return AppResult.Error(DataError.BadRequest)
        val credentials =
            AiCustomCredentialsModel(
                providerId.orEmpty(),
                name.trim().ifBlank { providerDomainName(normalizedUrl) },
                normalizedUrl,
                model.trim(),
                resolvedKey,
            )
        return when (
            val result =
                wordStudyRepository.generateWithCustomProvider(request, credentials)
        ) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Error -> AppResult.Error(result.error)
        }
    }
}

internal fun normalizeAiBaseUrl(value: String): String? {
    val normalized = value.trim().trimEnd('/')
    val url = runCatching { Url(normalized) }.getOrNull() ?: return null
    if (url.protocol.name != "https" || url.host.isBlank() || url.user != null || url.password != null ||
        url.parameters
            .isEmpty()
            .not() || url.fragment.isNotEmpty() ||
        url.encodedPath
            .trimEnd('/')
            .endsWith("/chat/completions", ignoreCase = true)
    ) {
        return null
    }
    return normalized
}

internal fun providerDomainName(baseUrl: String): String = runCatching { Url(baseUrl).host.removePrefix("www.") }.getOrDefault("")
