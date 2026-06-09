package com.arrazyfathan.kbbi.core.logging

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun currentTimestamp(): String {
    val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    return timestampFormat.format(Date())
}
