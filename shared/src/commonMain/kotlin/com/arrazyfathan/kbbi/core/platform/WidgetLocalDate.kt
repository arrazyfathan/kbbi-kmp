package com.arrazyfathan.kbbi.core.platform

data class WidgetLocalDate(
    val year: Int,
    val month: Int,
    val day: Int,
) {
    val dayOfYear: Int
        get() {
            val monthLengths =
                if (isLeapYear(year)) {
                    intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
                } else {
                    intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
                }
            return monthLengths.take(month - 1).sum() + day
        }

    fun toIsoString(): String =
        year.toString().padStart(4, '0') + "-" +
            month
                .toString()
                .padStart(2, '0') + "-" + day.toString().padStart(2, '0')

    companion object {
        private fun isLeapYear(year: Int): Boolean = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }
}

expect fun currentWidgetLocalDate(): WidgetLocalDate
