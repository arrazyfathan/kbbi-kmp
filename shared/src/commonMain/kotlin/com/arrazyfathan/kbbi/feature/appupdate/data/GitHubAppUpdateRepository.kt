package com.arrazyfathan.kbbi.feature.appupdate.data

import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateCheckResult
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdatePlatformMetadata
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateRepository
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateRequirement
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateTarget
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppVersionComparator
import com.arrazyfathan.kbbi.core.data.remote.network.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.Url
import kotlin.time.Clock

private const val LATEST_RELEASE_URL = "https://api.github.com/repos/arrazyfathan/kbbi/releases/latest"

internal class GitHubAppUpdateRepository(
    private val httpClient: HttpClient,
    private val metadata: AppUpdatePlatformMetadata,
    private val store: AppUpdateStore,
    private val currentTimeMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : AppUpdateRepository {
    override suspend fun checkForUpdate(force: Boolean): AppUpdateCheckResult {
        if (!metadata.enabled || metadata.currentVersion.isBlank()) return AppUpdateCheckResult.Disabled
        val cached = validCachedRequiredUpdate()
        if (!force && !store.shouldRunAutomaticCheck(currentTimeMillis())) {
            return cached?.let(AppUpdateCheckResult::Available) ?: AppUpdateCheckResult.UpToDate
        }

        return when (val result = safeCall<GitHubReleaseDto> { httpClient.get { url(LATEST_RELEASE_URL) } }) {
            is com.arrazyfathan.kbbi.core.domain.model.AppResult.Error -> {
                finish(force, successful = false)
                cached?.let(AppUpdateCheckResult::Available) ?: AppUpdateCheckResult.Unavailable
            }

            is com.arrazyfathan.kbbi.core.domain.model.AppResult.Success -> process(result.data, force, cached)
        }
    }

    private suspend fun process(
        release: GitHubReleaseDto,
        force: Boolean,
        cached: AppUpdate?,
    ): AppUpdateCheckResult {
        val version = AppVersionComparator.normalize(release.tagName) ?: return failed(force, cached)
        val current = AppVersionComparator.normalize(metadata.currentVersion) ?: return failed(force, cached)
        if (!isSafeReleaseUrl(release.htmlUrl)) return failed(force, cached)
        if (!AppVersionComparator.isNewer(version, current)) {
            store.clearRequiredUpdate()
            finish(force, successful = true)
            return AppUpdateCheckResult.UpToDate
        }

        val policy = loadPolicy(release.tagName, release.assets)
        if (!policy.successful && !AppVersionComparator.isMajorUpdate(version, current)) {
            if (cached != null) {
                finish(force, successful = false)
                return AppUpdateCheckResult.Available(cached)
            }
            return failed(force, null)
        }
        val targetAllowed = when {
            policy.targets == null -> metadata.target == AppUpdateTarget.ANDROID
            else -> metadata.target.policyKey in policy.targets
        }
        if (!targetAllowed) {
            finish(force, successful = policy.successful)
            return AppUpdateCheckResult.UpToDate
        }

        val actionUrl = when (metadata.target) {
            AppUpdateTarget.IOS -> metadata.releaseDestination?.takeIf(::isSafeHttpsUrl)
            AppUpdateTarget.WEB -> null
            AppUpdateTarget.ANDROID ->
                AppUpdateAssetSelector.select(
                    metadata.target,
                    version,
                    release.assets,
                    metadata.desktopPackageExtension,
                )?.takeIf(::isSafeReleaseAssetUrl) ?: release.htmlUrl
            AppUpdateTarget.DESKTOP ->
                AppUpdateAssetSelector.select(
                    metadata.target,
                    version,
                    release.assets,
                    metadata.desktopPackageExtension,
                )?.takeIf(::isSafeReleaseAssetUrl)
        }
        if (metadata.target != AppUpdateTarget.WEB && actionUrl == null) {
            finish(force, successful = policy.successful)
            return AppUpdateCheckResult.UpToDate
        }

        val requirement =
            if (AppVersionComparator.isMajorUpdate(version, current) || policy.forceUpdate) {
                AppUpdateRequirement.REQUIRED
            } else {
                AppUpdateRequirement.OPTIONAL
            }
        val update =
            AppUpdate(
                latestVersion = version,
                releaseUrl = release.htmlUrl,
                actionUrl = actionUrl,
                releaseNotes = release.body?.takeIf { it.isNotBlank() },
                requirement = requirement,
            )
        if (requirement == AppUpdateRequirement.REQUIRED) store.writeRequiredUpdate(update) else store.clearRequiredUpdate()
        finish(force, successful = policy.successful)
        return AppUpdateCheckResult.Available(update)
    }

    private suspend fun loadPolicy(releaseVersion: String, assets: List<GitHubReleaseAssetDto>): LoadedPolicy {
        val policyUrl = AppUpdateAssetSelector.selectPolicy(assets) ?: return LoadedPolicy(true, false, null)
        if (!isSafeReleaseAssetUrl(policyUrl)) return LoadedPolicy(false, false, null)
        return when (val response = safeCall<GitHubUpdatePolicyDto> { httpClient.get { url(policyUrl) } }) {
            is com.arrazyfathan.kbbi.core.domain.model.AppResult.Error -> LoadedPolicy(false, false, null)
            is com.arrazyfathan.kbbi.core.domain.model.AppResult.Success -> {
                val decoded = response.data
                val policyMatches =
                    decoded.schemaVersion == 1 &&
                        AppVersionComparator.compare(decoded.releaseVersion, releaseVersion) == 0
                LoadedPolicy(
                    successful = policyMatches,
                    forceUpdate = policyMatches && decoded.forceUpdate,
                    targets = decoded.targets?.map(String::lowercase)?.takeIf { policyMatches },
                )
            }
        }
    }

    private fun validCachedRequiredUpdate(): AppUpdate? {
        val cached = store.readRequiredUpdate() ?: return null
        if (cached.requirement == AppUpdateRequirement.REQUIRED &&
            AppVersionComparator.isNewer(cached.latestVersion, metadata.currentVersion) &&
            cached.actionUrl?.let(::isSafeHttpsUrl) != false
        ) return cached
        store.clearRequiredUpdate()
        return null
    }

    private fun failed(force: Boolean, cached: AppUpdate?): AppUpdateCheckResult {
        finish(force, successful = false)
        return cached?.let(AppUpdateCheckResult::Available) ?: AppUpdateCheckResult.Unavailable
    }

    private fun finish(force: Boolean, successful: Boolean) {
        if (!force) store.markAutomaticCheckFinished(currentTimeMillis(), successful)
    }

    private fun isSafeReleaseUrl(value: String): Boolean =
        runCatching { Url(value) }.getOrNull()?.let { it.protocol.name == "https" && it.host == "github.com" } == true

    private fun isSafeReleaseAssetUrl(value: String): Boolean =
        runCatching { Url(value) }.getOrNull()?.let {
            it.protocol.name == "https" && (it.host == "github.com" || it.host.endsWith(".githubusercontent.com"))
        } == true

    private fun isSafeHttpsUrl(value: String): Boolean =
        runCatching { Url(value) }.getOrNull()?.let {
            it.protocol.name == "https" && it.host.isNotBlank() && it.user.isNullOrEmpty() && it.password.isNullOrEmpty()
        } == true

    private data class LoadedPolicy(
        val successful: Boolean,
        val forceUpdate: Boolean,
        val targets: List<String>?,
    )
}
