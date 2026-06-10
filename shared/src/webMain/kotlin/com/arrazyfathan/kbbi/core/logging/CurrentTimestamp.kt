package com.arrazyfathan.kbbi.core.logging

import kotlin.time.Clock

actual fun currentTimestamp(): String = Clock.System.now().toString()
