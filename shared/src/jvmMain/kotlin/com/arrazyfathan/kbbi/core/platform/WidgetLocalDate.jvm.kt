package com.arrazyfathan.kbbi.core.platform

import java.time.LocalDate

actual fun currentWidgetLocalDate(): WidgetLocalDate = LocalDate.now().let { WidgetLocalDate(it.year, it.monthValue, it.dayOfMonth) }
