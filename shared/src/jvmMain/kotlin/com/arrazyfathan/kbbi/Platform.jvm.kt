package com.arrazyfathan.kbbi

import androidx.compose.runtime.Composable
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import org.koin.core.module.Module
import org.koin.dsl.module

private class DesktopPlatform : Platform {
    override val name: String = "Desktop JVM"
}

actual fun getPlatform(): Platform = DesktopPlatform()

actual val platformModule: Module =
    module {
        single<HttpClientEngine> { CIO.create() }
    }

@Composable
actual fun BindSystemBarColor(isDetailVisible: Boolean) = Unit

actual fun showToast(message: String) {
    println(message)
}

actual fun getAppVersionName(): String = "1.0.0"
