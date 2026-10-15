package com.arrazyfathan.kbbi.core.platform

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

actual fun currentWidgetLocalDate(): WidgetLocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.let {
        WidgetLocalDate(it.year, it.month.number, it.day)
    }
