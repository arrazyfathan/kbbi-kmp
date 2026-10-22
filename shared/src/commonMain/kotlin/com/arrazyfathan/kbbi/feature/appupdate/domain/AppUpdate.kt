package com.arrazyfathan.kbbi.feature.appupdate.domain

import kotlinx.serialization.Serializable

@Serializable
data class AppUpdate(
    val latestVersion: String,
    val releaseUrl: String,
    val actionUrl: String?,
    val releaseNotes: String?,
    val requirement: AppUpdateRequirement,
)

@Serializable
enum class AppUpdateRequirement {
    OPTIONAL,
    REQUIRED,
}

enum class AppUpdateTarget(
    val policyKey: String,
) {
    ANDROID("android"),
    IOS("ios"),
    DESKTOP("desktop"),
    WEB("web"),
}

data class AppUpdatePlatformMetadata(
    val target: AppUpdateTarget,
    val currentVersion: String,
    val enabled: Boolean,
    val releaseDestination: String? = null,
    val desktopPackageExtension: String? = null,
)

interface AppUpdateRepository {
    suspend fun checkForUpdate(force: Boolean = false): AppUpdateCheckResult
}

sealed interface AppUpdateCheckResult {
    data class Available(val update: AppUpdate) : AppUpdateCheckResult

    data object UpToDate : AppUpdateCheckResult

    data object Disabled : AppUpdateCheckResult

    data object Unavailable : AppUpdateCheckResult
}
