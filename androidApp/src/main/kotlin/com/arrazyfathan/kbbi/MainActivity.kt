package com.arrazyfathan.kbbi

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderScheduler
import com.arrazyfathan.kbbi.feature.settings.data.SettingsPreferenceStore
import com.arrazyfathan.kbbi.feature.settings.domain.ReminderType
import com.arrazyfathan.kbbi.notifications.AndroidIncomingLaunchRequests
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    private var pendingPermissionType: ReminderType? = null
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val type = pendingPermissionType
            pendingPermissionType = null
            if (type != null) {
                lifecycleScope.launch {
                    val koin = GlobalContext.get()
                    val preferences = koin.get<SettingsPreferenceStore>()
                    val current =
                        preferences.preferences.value.reminders
                            .getValue(type)
                    if (granted) {
                        koin.get<ReminderScheduler>().schedule(type)
                    } else {
                        preferences.setReminder(type, current.copy(enabled = false))
                        koin.get<ReminderScheduler>().cancel(type)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        current = this
        AndroidIncomingLaunchRequests.publish(intent)
        window.disableNavigationBarContrastEnforcement(Build.VERSION.SDK_INT)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        AndroidIncomingLaunchRequests.publish(intent)
    }

    override fun onResume() {
        super.onResume()
        current = this
    }

    override fun onDestroy() {
        if (current === this) current = null
        super.onDestroy()
    }

    fun requestReminderPermission(type: ReminderType) {
        if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onReminderPermissionResult(type, true)
            return
        }
        pendingPermissionType = type
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun onReminderPermissionResult(
        type: ReminderType,
        granted: Boolean,
    ) {
        lifecycleScope.launch {
            val koin = GlobalContext.get()
            val preferences = koin.get<SettingsPreferenceStore>()
            val currentPreference =
                preferences.preferences.value.reminders
                    .getValue(type)
            if (granted) {
                koin.get<ReminderScheduler>().schedule(type)
            } else {
                preferences.setReminder(type, currentPreference.copy(enabled = false))
                koin.get<ReminderScheduler>().cancel(type)
            }
        }
    }

    companion object {
        var current: MainActivity? = null
            private set
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
