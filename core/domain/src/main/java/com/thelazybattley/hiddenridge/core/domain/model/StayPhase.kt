package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Where a stay is relative to today, as front-desk staff think about it. */
sealed interface StayPhase {
    data object Departed : StayPhase

    data object LeavesToday : StayPhase

    data object ArrivesToday : StayPhase

    /** Tonight is night [night] of [nights]. */
    data class InHouse(val night: Int, val nights: Int) : StayPhase

    data class ArrivesIn(val days: Int) : StayPhase
}

fun Booking.stayPhaseOn(today: LocalDate): StayPhase = when {
    checkOut.isBefore(today) -> StayPhase.Departed
    checkOut == today -> StayPhase.LeavesToday
    checkIn == today -> StayPhase.ArrivesToday
    checkIn.isBefore(today) -> StayPhase.InHouse(
        night = ChronoUnit.DAYS.between(checkIn, today).toInt() + 1,
        nights = nights,
    )
    else -> StayPhase.ArrivesIn(days = ChronoUnit.DAYS.between(today, checkIn).toInt())
}
