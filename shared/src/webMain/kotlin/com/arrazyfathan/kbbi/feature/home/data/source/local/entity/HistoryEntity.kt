package com.arrazyfathan.kbbi.feature.home.data.source.local.entity

import kotlin.time.Clock

actual fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
