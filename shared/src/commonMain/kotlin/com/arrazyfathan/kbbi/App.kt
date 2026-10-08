package com.arrazyfathan.kbbi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.feature.splash.presentation.navigation.SplashRoute
import com.arrazyfathan.kbbi.navigation.MainApp

@Composable
fun App() {
    KBBITheme {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        ) {
            Box(
                modifier = Modifier.align(Alignment.Center).widthIn(max = 840.dp).fillMaxSize(),
            ) {
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
    }
}
