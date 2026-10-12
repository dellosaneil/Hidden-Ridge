package com.thelazybattley.hiddenridge.feature.timeline.components

import androidx.annotation.StringRes
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.feature.timeline.R
import java.time.format.DateTimeFormatter
import java.util.Locale

internal object TimelineFormat {
    fun monthYear(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)

    fun shortMonth(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", locale)

    fun shortYear(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("yy", locale)

    fun monthDay(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", locale)

    fun fullDate(locale: Locale): DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", locale)
}

@get:StringRes
internal val HouseType.groupLabel: Int
    get() = when (this) {
        HouseType.CABIN -> R.string.timeline_group_cabins
        HouseType.CASITA -> R.string.timeline_group_casitas
        HouseType.FAMILY_HOUSE -> R.string.timeline_group_family_houses
    }
