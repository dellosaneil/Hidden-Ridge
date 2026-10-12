package com.thelazybattley.hiddenridge.feature.timeline.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.feature.timeline.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
@ReadOnlyComposable
internal fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

internal object TimelineFormat {
    fun monthYear(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)

    fun shortMonth(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", locale)

    fun shortYear(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("yy", locale)

    fun monthDay(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", locale)

    fun weekdayDayMonth(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM", locale)

    fun fullDate(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", locale)

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

@get:StringRes
internal val HouseType.groupLabel: Int
    get() = when (this) {
        HouseType.CABIN -> R.string.timeline_group_cabins
        HouseType.CASITA -> R.string.timeline_group_casitas
        HouseType.FAMILY_HOUSE -> R.string.timeline_group_family_houses
    }
