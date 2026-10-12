package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate

/** A booking with the houses it occupies resolved, as seen on [today]. */
data class BookingDetails(
    val booking: Booking,
    /** Every house for a whole-resort booking; otherwise the booked houses in listing order. */
    val houses: List<House>,
    val today: LocalDate,
) {
    val stayPhase: StayPhase get() = booking.stayPhaseOn(today)

    val totalSleeps: Int get() = houses.sumOf(House::sleeps)
}
