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

    /** Open for the whole stay and not booked by anyone else. */
    fun isFree(houseId: String, checkIn: LocalDate, checkOut: LocalDate): Boolean =
        houses.firstOrNull { it.id == houseId }?.isOpenOn(checkIn) != false &&
            conflictsFor(StayRequest(BookingMode.INDIVIDUAL, listOf(houseId), checkIn, checkOut)).isEmpty()

    /**
     * The requested houses that aren't bookable yet when the stay starts. A whole-resort stay
     * takes whichever houses are open, so it's never stopped by one that isn't.
     */
    fun closedFor(request: StayRequest): List<House> =
        if (request.isWholeResort) emptyList() else houses.filter { it.id in request.houseIds && !it.isOpenOn(request.checkIn) }

    /**
     * Nights in [from] until [until] that can't be booked for what's chosen so far: with no house
     * chosen yet, only nights when every house is taken; otherwise any night a chosen house is taken.
     */
    fun blockedNights(mode: BookingMode, houseIds: List<String>, from: LocalDate, until: LocalDate): Set<LocalDate> {
        val nights = generateSequence(from) { it.plusDays(1) }.takeWhile { it.isBefore(until) }
        if (mode == BookingMode.INDIVIDUAL && houseIds.isEmpty()) {
            return nights.filter { night -> housesTakenOn(night) >= houses.size }.toSet()
        }
        val chosen = if (mode == BookingMode.WHOLE_RESORT) emptyList() else houses.filter { it.id in houseIds }
        val relevant = if (mode == BookingMode.WHOLE_RESORT) {
            bookings
        } else {
            bookings.filter { it.isWholeResort || it.houseIds.any(houseIds::contains) }
        }
        return nights.filter { night -> relevant.any { it.coversNight(night) } || chosen.any { !it.isOpenOn(night) } }.toSet()
    }

    /** Houses booked or not open yet on [night]. */
    private fun housesTakenOn(night: LocalDate): Int {
        val staying = bookings.filter { it.coversNight(night) }
        if (staying.any(Booking::isWholeResort)) return houses.size
        val booked = staying.flatMap(Booking::houseIds).toSet()
        return houses.count { it.id in booked || !it.isOpenOn(night) }
    }
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
