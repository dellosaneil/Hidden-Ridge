package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate

/** Every house and the active bookings inside [window], for deciding what can still be booked. */
data class Availability(
    val today: LocalDate,
    val window: DateWindow,
    val houses: List<House>,
    val bookings: List<Booking>,
) {
    fun conflictsFor(request: StayRequest): List<BookingConflict> = bookings.conflictsWith(request)

    fun isFree(houseId: String, checkIn: LocalDate, checkOut: LocalDate): Boolean =
        conflictsFor(StayRequest(BookingMode.INDIVIDUAL, listOf(houseId), checkIn, checkOut)).isEmpty()

    /**
     * Nights in [from] until [until] that can't be booked for what's chosen so far: with no house
     * chosen yet, only nights when every house is taken; otherwise any night a chosen house is taken.
     */
    fun blockedNights(mode: BookingMode, houseIds: List<String>, from: LocalDate, until: LocalDate): Set<LocalDate> {
        val nights = generateSequence(from) { it.plusDays(1) }.takeWhile { it.isBefore(until) }
        if (mode == BookingMode.INDIVIDUAL && houseIds.isEmpty()) {
            return nights.filter { night -> housesTakenOn(night) >= houses.size }.toSet()
        }
        val relevant = if (mode == BookingMode.WHOLE_RESORT) {
            bookings
        } else {
            bookings.filter { it.isWholeResort || it.houseIds.any(houseIds::contains) }
        }
        return nights.filter { night -> relevant.any { it.coversNight(night) } }.toSet()
    }

    private fun housesTakenOn(night: LocalDate): Int =
        bookings.filter { it.coversNight(night) }.sumOf { if (it.isWholeResort) houses.size else it.houseIds.size }
}

/** A stay someone wants to book, checked against existing bookings before it exists. */
data class StayRequest(
    val mode: BookingMode,
    val houseIds: List<String>,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
) {
    val isWholeResort: Boolean get() = mode == BookingMode.WHOLE_RESORT
}

/** An existing [booking] that already uses some of the requested nights in [houseIds]. */
data class BookingConflict(
    val booking: Booking,
    /** The houses both want; empty when [booking] takes the whole resort. */
    val houseIds: List<String>,
)

/** A whole-resort stay clashes with anything that overlaps; otherwise only bookings sharing a house do. */
fun List<Booking>.conflictsWith(request: StayRequest): List<BookingConflict> = mapNotNull { booking ->
    when {
        !booking.overlaps(request.checkIn, request.checkOut) -> null
        request.isWholeResort || booking.isWholeResort ->
            BookingConflict(booking, if (request.isWholeResort) booking.houseIds else request.houseIds)
        else -> request.houseIds.filter(booking.houseIds::contains)
            .takeIf { it.isNotEmpty() }
            ?.let { BookingConflict(booking, it) }
    }
}
