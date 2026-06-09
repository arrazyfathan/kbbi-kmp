package com.arrazyfathan.kbbi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.feature.splash.presentation.navigation.SplashRoute
import com.arrazyfathan.kbbi.navigation.MainApp

@Composable
fun App() {
    KBBITheme {
        var isSplashVisible by rememberSaveable { mutableStateOf(true) }

        if (isSplashVisible) {
            SplashRoute(
                onTimeout = {
                    isSplashVisible = false
                },
            )
        } else {
            MainApp()
        }
    }
}