package com.arrazyfathan.kbbi.feature.appupdate.domain

import com.arrazyfathan.kbbi.core.platform.CapabilityResult

fun interface AppUpdateLauncher {
    fun launch(update: AppUpdate): CapabilityResult<Unit>
}
