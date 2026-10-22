package com.arrazyfathan.kbbi.feature.appupdate.data

import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateTarget
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppVersionComparator

object AppUpdateAssetSelector {
    fun select(
        target: AppUpdateTarget,
        version: String,
        assets: List<GitHubReleaseAssetDto>,
        desktopPackageExtension: String? = null,
    ): String? {
        val names = assets.associateBy { it.name.lowercase() }
        return when (target) {
            AppUpdateTarget.ANDROID -> {
                val normalized = AppVersionComparator.normalize(version) ?: return null
                val expected = "kbbi-v$normalized-release.apk"
                names[expected]?.browserDownloadUrl
                    ?: assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }?.browserDownloadUrl
            }

            AppUpdateTarget.IOS -> null
            AppUpdateTarget.WEB -> null
            AppUpdateTarget.DESKTOP -> {
                val extension = desktopPackageExtension?.removePrefix(".")?.lowercase() ?: return null
                assets.firstOrNull { it.name.endsWith(".$extension", ignoreCase = true) }?.browserDownloadUrl
            }
        }
    }

    fun selectPolicy(assets: List<GitHubReleaseAssetDto>): String? =
        assets.firstOrNull { it.name.equals(POLICY_ASSET_NAME, ignoreCase = true) }?.browserDownloadUrl

    const val POLICY_ASSET_NAME = "kbbi-update-policy.json"
}
