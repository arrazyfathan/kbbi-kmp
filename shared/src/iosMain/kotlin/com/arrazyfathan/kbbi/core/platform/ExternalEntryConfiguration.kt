package com.arrazyfathan.kbbi.core.platform

import com.arrazyfathan.kbbi.shared.BuildKonfig

/** Exposes only the public dictionary API origin to the native share extension host. */
fun getShareExtensionApiBaseUrl(): String = BuildKonfig.BASE_URL
