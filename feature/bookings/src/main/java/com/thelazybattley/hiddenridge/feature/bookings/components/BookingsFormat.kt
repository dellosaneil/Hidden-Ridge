package com.thelazybattley.hiddenridge.feature.bookings.components

import com.thelazybattley.hiddenridge.core.designsystem.format.DateFormats
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal object BookingsFormat {
    /** "Mon 12 Oct" */
    fun weekdayDayMonth(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM", locale)

    fun shortMonth(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", locale)

    fun month(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM", locale)

    fun monthYear(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)

    /** "Wed 14 – Sat 17", for stays this week. */
    fun weekdayRange(checkIn: LocalDate, checkOut: LocalDate, locale: Locale): String {
        val format = DateTimeFormatter.ofPattern("EEE d", locale)
        return "${format.format(checkIn)} – ${format.format(checkOut)}"
    }

    /** "12–15 Oct" or "30 Oct – 2 Nov" in [currentYear]; with years otherwise. */
    fun shortRange(checkIn: LocalDate, checkOut: LocalDate, currentYear: Int, locale: Locale): String {
        if (checkIn.year != currentYear || checkOut.year != currentYear) return DateFormats.stayRange(checkIn, checkOut, locale)
        val dayMonth = DateTimeFormatter.ofPattern("d MMM", locale)
        return if (checkIn.month == checkOut.month) {
            "${checkIn.dayOfMonth}–${dayMonth.format(checkOut)}"
        } else {
            "${dayMonth.format(checkIn)} – ${dayMonth.format(checkOut)}"
        }
    }
}
