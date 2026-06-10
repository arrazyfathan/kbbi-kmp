package com.arrazyfathan.kbbi

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.arrazyfathan.kbbi.di.initKoin

fun main() {
    AppLogger.plantDebugTree()
    initKoin()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KBBI",
        ) {
            App()
        }
    }
}
