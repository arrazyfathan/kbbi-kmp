package com.arrazyfathan.kbbi

import androidx.compose.ui.window.ComposeUIViewController
import com.arrazyfathan.kbbi.di.initKoin

private var isKoinInitialized = false

@Suppress("FunctionName")
fun MainViewController() =
    run {
        if (!isKoinInitialized) {
            initKoin()
            isKoinInitialized = true
        }
        ComposeUIViewController { App() }
    }
