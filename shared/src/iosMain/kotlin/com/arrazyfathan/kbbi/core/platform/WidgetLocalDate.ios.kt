@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.arrazyfathan.kbbi.core.platform

import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale

actual fun currentWidgetLocalDate(): WidgetLocalDate {
    val formatter =
        NSDateFormatter().apply {
            dateFormat = "yyyy-MM-dd"
            locale = NSLocale(localeIdentifier = "en_US_POSIX")
            timeZone = NSCalendar.currentCalendar.timeZone
        }
    val parts = formatter.stringFromDate(NSDate()).split('-')
    return WidgetLocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
}
