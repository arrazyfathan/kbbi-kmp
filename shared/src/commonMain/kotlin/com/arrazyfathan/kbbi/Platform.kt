package com.arrazyfathan.kbbi

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect val platformModule: Module

@Composable
expect fun BindSystemBarColor(isDetailVisible: Boolean)

expect fun showToast(message: String)

expect fun getAppVersionName(): String
