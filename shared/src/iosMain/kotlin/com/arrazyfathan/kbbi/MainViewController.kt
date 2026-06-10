package com.arrazyfathan.kbbi

import androidx.compose.ui.window.ComposeUIViewController

@Suppress("FunctionName")
fun MainViewController() =
    run {
        ComposeUIViewController { App() }
    }
