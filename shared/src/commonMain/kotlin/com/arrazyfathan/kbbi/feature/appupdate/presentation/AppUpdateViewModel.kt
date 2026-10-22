package com.arrazyfathan.kbbi.feature.appupdate.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.platform.CapabilityResult
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateCheckResult
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateRepository
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdatePlatformMetadata
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateLauncher
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateRequirement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUpdateState(
    val currentVersion: String,
    val availableUpdate: AppUpdate? = null,
    val isChecking: Boolean = false,
    val manualCheckResult: ManualUpdateCheckResult? = null,
    val actionFailed: Boolean = false,
)

enum class ManualUpdateCheckResult { UP_TO_DATE, UNAVAILABLE }

class AppUpdateViewModel(
    private val repository: AppUpdateRepository,
    private val launcher: AppUpdateLauncher,
    metadata: AppUpdatePlatformMetadata,
) : ViewModel() {
    private val _state = MutableStateFlow(AppUpdateState(metadata.currentVersion))
    val state = _state.asStateFlow()
    private var hasStarted = false

    fun onAppStarted() {
        if (hasStarted) return
        hasStarted = true
        check(force = false, manual = false)
    }

    fun checkManually() {
        check(force = true, manual = true)
    }

    fun dismissPrompt() {
        if (_state.value.availableUpdate?.requirement == AppUpdateRequirement.REQUIRED) return
        _state.update { it.copy(availableUpdate = null) }
    }

    fun launchUpdate() {
        val update = _state.value.availableUpdate ?: return
        _state.update { it.copy(actionFailed = false) }
        if (launcher.launch(update) !is CapabilityResult.Success) {
            _state.update { it.copy(actionFailed = true) }
        }
    }

    private fun check(force: Boolean, manual: Boolean) {
        if (_state.value.isChecking) return
        viewModelScope.launch {
            _state.update { it.copy(isChecking = true, manualCheckResult = null) }
            val result = repository.checkForUpdate(force)
            _state.update { current ->
                current.copy(
                    availableUpdate = when (result) {
                        is AppUpdateCheckResult.Available -> result.update
                        AppUpdateCheckResult.UpToDate, AppUpdateCheckResult.Disabled -> null
                        AppUpdateCheckResult.Unavailable -> current.availableUpdate
                    },
                    isChecking = false,
                    manualCheckResult =
                        if (manual) {
                            when (result) {
                                AppUpdateCheckResult.UpToDate, AppUpdateCheckResult.Disabled -> ManualUpdateCheckResult.UP_TO_DATE
                                AppUpdateCheckResult.Unavailable -> ManualUpdateCheckResult.UNAVAILABLE
                                is AppUpdateCheckResult.Available -> null
                            }
                        } else {
                            null
                        },
                )
            }
        }
    }
}
