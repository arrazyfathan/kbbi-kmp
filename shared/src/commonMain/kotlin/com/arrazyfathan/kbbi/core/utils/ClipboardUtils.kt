package com.arrazyfathan.kbbi.core.utils

import androidx.compose.ui.platform.Clipboard

expect suspend fun Clipboard.setPlainText(text: String)
