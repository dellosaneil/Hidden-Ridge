package com.thelazybattley.hiddenridge.core.designsystem.format

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The locale to format with; reading it here recomposes when the user changes language. */
@Composable
@ReadOnlyComposable
fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

object DateFormats {
    /** "Sat, 10 Oct" */
    fun weekdayDayMonth(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", locale)

    /** "Wed, 14 Oct 2026" */
    fun weekdayDayMonthYear(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", locale)

    /** "10–14 Oct 2026", "30 Oct – 2 Nov 2026" or "28 Dec 2026 – 1 Jan 2027". */
    fun stayRange(checkIn: LocalDate, checkOut: LocalDate, locale: Locale): String {
        val dayMonthYear = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
        return when {
            checkIn.year != checkOut.year -> "${dayMonthYear.format(checkIn)} – ${dayMonthYear.format(checkOut)}"
            checkIn.month != checkOut.month ->
                "${DateTimeFormatter.ofPattern("d MMM", locale).format(checkIn)} – ${dayMonthYear.format(checkOut)}"
            else -> "${checkIn.dayOfMonth}–${dayMonthYear.format(checkOut)}"
        }
    }
}
