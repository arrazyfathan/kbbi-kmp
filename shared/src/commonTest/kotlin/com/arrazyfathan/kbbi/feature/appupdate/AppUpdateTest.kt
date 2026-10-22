package com.arrazyfathan.kbbi.feature.appupdate

import com.arrazyfathan.kbbi.feature.appupdate.data.AppUpdateAssetSelector
import com.arrazyfathan.kbbi.feature.appupdate.data.AppUpdateStore
import com.arrazyfathan.kbbi.feature.appupdate.data.GitHubAppUpdateRepository
import com.arrazyfathan.kbbi.feature.appupdate.data.GitHubReleaseAssetDto
import com.arrazyfathan.kbbi.feature.appupdate.data.GitHubReleaseDto
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateCheckResult
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdatePlatformMetadata
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateRequirement
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateTarget
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppVersionComparator
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AppUpdateTest {
    @Test
    fun versionComparisonHandlesNumericOrderingAndRejectsMalformedVersions() {
        assertEquals(1, AppVersionComparator.compare("v1.10.0", "1.9.9"))
        assertEquals(0, AppVersionComparator.compare("1.2", "1.2.0"))
        assertEquals(null, AppVersionComparator.compare("1.x", "1.0"))
        assertEquals(false, AppVersionComparator.isNewer("1.2.0", "1.2.0"))
    }

    @Test
    fun assetSelectionNeverReturnsAnArtifactForAnotherTarget() {
        val assets =
            listOf(
                GitHubReleaseAssetDto("kbbi-v2.0.0-release.apk", "https://github.com/a.apk"),
                GitHubReleaseAssetDto("KBBI-2.0.0.dmg", "https://github.com/a.dmg"),
            )
        assertEquals("https://github.com/a.apk", AppUpdateAssetSelector.select(AppUpdateTarget.ANDROID, "2.0.0", assets))
        assertEquals("https://github.com/a.dmg", AppUpdateAssetSelector.select(AppUpdateTarget.DESKTOP, "2.0.0", assets, "dmg"))
        assertEquals(null, AppUpdateAssetSelector.select(AppUpdateTarget.IOS, "2.0.0", assets))
        assertEquals(null, AppUpdateAssetSelector.select(AppUpdateTarget.WEB, "2.0.0", assets))
    }

    @Test
    fun newerAndroidReleaseUsesApkAndRequiredPolicy() = runTest {
        val client = client { request ->
            if (request.url.host == "api.github.com") {
                respond(releaseJson(assets = apkAndPolicyAssets), headers = jsonHeaders)
            } else {
                respond("""{"schemaVersion":1,"releaseVersion":"2.0.0","forceUpdate":true}""", headers = jsonHeaders)
            }
        }
        val repository = repository(client, AppUpdateTarget.ANDROID)
        val result = assertIs<AppUpdateCheckResult.Available>(repository.checkForUpdate(force = true))
        assertEquals(AppUpdateRequirement.REQUIRED, result.update.requirement)
        assertEquals("https://github.com/arrazyfathan/kbbi/releases/download/v2.0.0/kbbi-v2.0.0-release.apk", result.update.actionUrl)
        client.close()
    }

    @Test
    fun missingAndroidAssetFallsBackToReleaseNotesInsteadOfAnotherPackage() = runTest {
        val client = client { respond(releaseJson(assets = emptyList()), headers = jsonHeaders) }
        val result = assertIs<AppUpdateCheckResult.Available>(repository(client, AppUpdateTarget.ANDROID).checkForUpdate(force = true))
        assertEquals("https://github.com/arrazyfathan/kbbi/releases/tag/v2.0.0", result.update.actionUrl)
        client.close()
    }

    @Test
    fun iosRequiresAnExplicitTargetPolicyAndUsesConfiguredStoreDestination() = runTest {
        val client = client { request ->
            if (request.url.host == "api.github.com") {
                respond(releaseJson(assets = apkAndPolicyAssets), headers = jsonHeaders)
            } else {
                respond("""{"schemaVersion":1,"releaseVersion":"2.0.0","forceUpdate":false,"targets":["android"]}""", headers = jsonHeaders)
            }
        }
        assertEquals(
            AppUpdateCheckResult.UpToDate,
            repository(client, AppUpdateTarget.IOS).checkForUpdate(force = true),
        )
        client.close()
    }

    @Test
    fun policyFailureKeepsAStillNewerCachedRequiredUpdate() = runTest {
        val cached =
            AppUpdate(
                latestVersion = "1.3.0",
                releaseUrl = "https://github.com/arrazyfathan/kbbi/releases/tag/v1.3.0",
                actionUrl = "https://github.com/arrazyfathan/kbbi/releases/download/v1.3.0/app.apk",
                releaseNotes = "Required fix",
                requirement = AppUpdateRequirement.REQUIRED,
            )
        val store = FakeStore(cached)
        val client = client { request ->
            if (request.url.host == "api.github.com") {
                respond(
                    releaseJson(
                        version = "1.3.0",
                        assets =
                            listOf(
                                GitHubReleaseAssetDto(
                                    "kbbi-v1.3.0-release.apk",
                                    "https://github.com/arrazyfathan/kbbi/releases/download/v1.3.0/kbbi-v1.3.0-release.apk",
                                ),
                                GitHubReleaseAssetDto(
                                    "kbbi-update-policy.json",
                                    "https://github.com/arrazyfathan/kbbi/releases/download/v1.3.0/kbbi-update-policy.json",
                                ),
                            ),
                    ),
                    headers = jsonHeaders,
                )
            } else {
                respond("unavailable", status = HttpStatusCode.ServiceUnavailable)
            }
        }
        val result =
            GitHubAppUpdateRepository(
                client,
                metadata(AppUpdateTarget.ANDROID),
                store,
            ).checkForUpdate(force = true)
        assertEquals(cached, assertIs<AppUpdateCheckResult.Available>(result).update)
        client.close()
    }

    @Test
    fun malformedReleaseDoesNotCreateAnUpdate() = runTest {
        val client = client {
            respond(
                """{"tag_name":"release-unknown","html_url":"https://github.com/arrazyfathan/kbbi/releases/tag/bad","assets":[]}""",
                headers = jsonHeaders,
            )
        }
        assertEquals(AppUpdateCheckResult.Unavailable, repository(client, AppUpdateTarget.ANDROID).checkForUpdate(force = true))
        client.close()
    }

    private fun repository(client: HttpClient, target: AppUpdateTarget) =
        GitHubAppUpdateRepository(client, metadata(target), FakeStore())

    private fun metadata(target: AppUpdateTarget) =
        AppUpdatePlatformMetadata(
            target = target,
            currentVersion = "1.2.0",
            enabled = true,
            releaseDestination = "https://testflight.apple.com/join/example",
            desktopPackageExtension = "dmg",
        )

    private fun client(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        HttpClient(MockEngine(handler)) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

    private fun releaseJson(version: String = "2.0.0", assets: List<GitHubReleaseAssetDto>): String =
        Json.encodeToString(
            GitHubReleaseDto(
                tagName = "v$version",
                htmlUrl = "https://github.com/arrazyfathan/kbbi/releases/tag/v$version",
                body = "Release notes",
                assets = assets,
            ),
        )

    private class FakeStore(
        private var cached: AppUpdate? = null,
    ) : AppUpdateStore {
        override fun shouldRunAutomaticCheck(nowMillis: Long) = true
        override fun markAutomaticCheckFinished(nowMillis: Long, successful: Boolean) = Unit
        override fun readRequiredUpdate() = cached
        override fun writeRequiredUpdate(update: AppUpdate) { cached = update }
        override fun clearRequiredUpdate() { cached = null }
    }

    private companion object {
        val jsonHeaders = io.ktor.http.headersOf(HttpHeaders.ContentType, "application/json")
        val apkAndPolicyAssets =
            listOf(
                GitHubReleaseAssetDto(
                    "kbbi-v2.0.0-release.apk",
                    "https://github.com/arrazyfathan/kbbi/releases/download/v2.0.0/kbbi-v2.0.0-release.apk",
                ),
                GitHubReleaseAssetDto(
                    "kbbi-update-policy.json",
                    "https://github.com/arrazyfathan/kbbi/releases/download/v2.0.0/kbbi-update-policy.json",
                ),
            )
    }
}
