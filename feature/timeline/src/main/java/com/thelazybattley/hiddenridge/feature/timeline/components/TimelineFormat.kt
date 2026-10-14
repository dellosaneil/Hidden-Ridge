package com.thelazybattley.hiddenridge.feature.timeline.components

import java.time.format.DateTimeFormatter
import java.util.Locale

internal object TimelineFormat {
    fun monthYear(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)

    fun shortMonth(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", locale)

    fun shortYear(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("yy", locale)

    fun monthDay(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", locale)

    fun fullDate(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", locale)
}
