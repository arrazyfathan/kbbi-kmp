package com.arrazyfathan.kbbi

import androidx.compose.runtime.Composable
import org.koin.core.module.Module
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdatePlatformMetadata

interface Platform {
    val name: String
    val supportsNativeToast: Boolean get() = false
}

expect fun getPlatform(): Platform

expect val platformModule: Module

@Composable
expect fun BindSystemBarColor(isDetailVisible: Boolean)

expect fun showToast(message: String)

expect fun getAppVersionName(): String

expect fun getAppUpdatePlatformMetadata(): AppUpdatePlatformMetadata
