package com.arrazyfathan.kbbi.core.logging

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale

actual fun currentTimestamp(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    formatter.locale = NSLocale("en_US")
    return formatter.stringFromDate(NSDate())
}
