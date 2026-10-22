package com.arrazyfathan.kbbi.feature.appupdate.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubReleaseDto(
    @SerialName("tag_name") val tagName: String,
    @SerialName("html_url") val htmlUrl: String,
    val body: String? = null,
    val assets: List<GitHubReleaseAssetDto> = emptyList(),
)

@Serializable
data class GitHubReleaseAssetDto(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
)

@Serializable
data class GitHubUpdatePolicyDto(
    val schemaVersion: Int,
    val releaseVersion: String,
    val forceUpdate: Boolean,
    /** Older release policies are Android-only; other targets require explicit metadata. */
    val targets: List<String>? = null,
)
