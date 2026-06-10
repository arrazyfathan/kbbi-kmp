package com.arrazyfathan.kbbi

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.arrazyfathan.kbbi.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    AppLogger.plantDebugTree()
    initKoin()

    ComposeViewport {
        App()
    }
}
