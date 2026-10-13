package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Why a move can't be saved. */
enum class MoveProblem {
    /** Cancelled, or already checked out. */
    NOT_MOVABLE,

    /** Too early (before today, or earlier at all once the guest has arrived), or past the bookable window. */
    OUT_OF_RANGE,

    /** Only a booking in a single house can change house. */
    HOUSE_LOCKED,
    NO_HOUSES,

    /** The same dates and houses it already has. */
    UNCHANGED,
}

/** Whether staff can still move this stay: it isn't cancelled and has nights left. */
fun Booking.canMoveOn(today: LocalDate): Boolean = status != BookingStatus.CANCELLED && checkOut.isAfter(today)

/**
 * Whether a move can put this booking in another house as well as on other dates. Whole-resort
 * stays already have every house, and a group keeps its set of houses together.
 */
val Booking.canChangeHouse: Boolean get() = !isWholeResort && houseIds.size == 1

/**
 * How many days earlier (negative) or later this stay can shift, ending no later than [until].
 * A future stay can come forward as far as today; one already under way can only go later, since
 * the nights already stayed can't be undone.
 */
fun Booking.allowedShift(today: LocalDate, until: LocalDate): IntRange {
    val earliest = if (checkIn.isBefore(today)) 0 else -ChronoUnit.DAYS.between(today, checkIn).toInt()
    val latest = ChronoUnit.DAYS.between(checkOut, until).toInt()
    return earliest..latest
}

/** This booking on new dates, the same length, and in [houseIds] unless it takes the whole resort. */
fun Booking.movedTo(checkIn: LocalDate, houseIds: List<String>): Booking = copy(
    checkIn = checkIn,
    checkOut = checkIn.plusDays(nights.toLong()),
    houseIds = if (isWholeResort) emptyList() else houseIds,
    // A hold is never kept past check-in.
    holdUntil = holdUntil?.let { minOf(it, checkIn) },
)

fun Booking.moveProblem(checkIn: LocalDate, houseIds: List<String>, today: LocalDate, until: LocalDate): MoveProblem? {
    val shift = ChronoUnit.DAYS.between(this.checkIn, checkIn).toInt()
    return when {
        !canMoveOn(today) -> MoveProblem.NOT_MOVABLE
        shift !in allowedShift(today, until) -> MoveProblem.OUT_OF_RANGE
        isWholeResort -> if (shift == 0) MoveProblem.UNCHANGED else null
        houseIds.isEmpty() -> MoveProblem.NO_HOUSES
        houseIds.toSet() != this.houseIds.toSet() && !canChangeHouse -> MoveProblem.HOUSE_LOCKED
        shift == 0 && houseIds.toSet() == this.houseIds.toSet() -> MoveProblem.UNCHANGED
        else -> null
    }
}

/**
 * Bookings that already use any night of [moved] in its houses, ignoring the booking itself.
 * [moved] is a booking after [movedTo], so it keeps its id.
 */
fun List<Booking>.conflictsForMove(moved: Booking): List<BookingConflict> =
    filter { it.id != moved.id }
        .conflictsWith(StayRequest(moved.mode, moved.houseIds, moved.checkIn, moved.checkOut))
