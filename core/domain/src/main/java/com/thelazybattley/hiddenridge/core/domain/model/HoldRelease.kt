package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate

/** When an unconfirmed hold gives its houses back. */
object HoldRelease {
    const val DEFAULT_DAYS = 3

    /** [afterDays] from today, but never past check-in and never before today. */
    fun date(today: LocalDate, checkIn: LocalDate, afterDays: Int = DEFAULT_DAYS): LocalDate =
        minOf(today.plusDays(afterDays.toLong()), checkIn).coerceAtLeast(today)
}
