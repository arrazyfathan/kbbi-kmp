package com.arrazyfathan.kbbi

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        window.disableNavigationBarContrastEnforcement(Build.VERSION.SDK_INT)

        setContent {
            App()
        }
    }
}

@SuppressLint("NewApi")
private fun Window.disableNavigationBarContrastEnforcement(sdkInt: Int) {
    if (sdkInt >= Build.VERSION_CODES.Q) isNavigationBarContrastEnforced = false
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
