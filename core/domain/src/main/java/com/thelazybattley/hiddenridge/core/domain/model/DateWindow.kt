package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** A run of whole days, [start] inclusive to [endExclusive] exclusive. */
data class DateWindow(
    val start: LocalDate,
    val endExclusive: LocalDate,
) {
    init {
        require(endExclusive.isAfter(start)) { "Window must contain at least one day" }
    }

    val dayCount: Int get() = ChronoUnit.DAYS.between(start, endExclusive).toInt()

    /** Days from [start] to [date]; negative before the window. */
    fun indexOf(date: LocalDate): Int = ChronoUnit.DAYS.between(start, date).toInt()

    fun dateAt(index: Int): LocalDate = start.plusDays(index.toLong())

    companion object {
        private const val MONTHS_BEFORE_TODAY = 1L
        private const val MONTHS_SHOWN = 27L

        /**
         * The window the availability timeline covers: from the start of last month, so in-house
         * stays show where they began, to a little over two years ahead for long-lead group bookings.
         */
        fun timelineAround(today: LocalDate): DateWindow {
            val start = today.withDayOfMonth(1).minusMonths(MONTHS_BEFORE_TODAY)
            return DateWindow(start = start, endExclusive = start.plusMonths(MONTHS_SHOWN))
        }
    }
}
