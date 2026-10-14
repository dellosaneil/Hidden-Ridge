package com.thelazybattley.hiddenridge.feature.houses.components

import java.time.format.DateTimeFormatter
import java.util.Locale

internal object HousesFormat {
    /** "Wed 14" */
    fun weekdayDay(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d", locale)

    /** "Mon 12 Oct" */
    fun weekdayDayMonth(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM", locale)
}
