package com.thelazybattley.hiddenridge.feature.overview

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.usecase.BookingLog
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

private const val NIGHTS_AHEAD = 7L
private const val MONTHS_SHOWN = 3L

/** A hold released within this many days counts as releasing soon. */
private const val SOON_DAYS = 3L

internal fun BookingLog.toOverview(): OverviewUi {
    val active = bookings.filter { it.status != BookingStatus.CANCELLED }
    val housesById = houses.associateBy(House::id)
    val arrivals = active.filter { it.checkIn == today }.map { it.toMove(housesById, isArrival = true) }
    val departures = active.filter { it.checkOut == today }.map { it.toMove(housesById, isArrival = false) }
    val holds = active.filter { it.isOnHold && it.checkOut.isAfter(today) }
    val months = generateSequence(YearMonth.from(today)) { it.plusMonths(1) }.take(MONTHS_SHOWN.toInt()).toList()
    val monthsEnd = months.last().atEndOfMonth().plusDays(1)

    return OverviewUi(
        today = today,
        houseCount = houses.size,
        occupiedTonight = takenOn(today, active).let { (booked, held) -> booked + held },
        arrivals = arrivals,
        departures = departures,
        holds = HoldSummary(
            count = holds.size,
            releasingSoon = holds.count { it.holdUntil?.let { until -> !until.isAfter(today.plusDays(SOON_DAYS)) } == true },
            noLimit = holds.count { it.holdUntil == null },
            later = holds.count { it.holdUntil?.isAfter(today.plusDays(SOON_DAYS)) == true },
        ),
        pendingMoves = (arrivals + departures).filterNot(MovementUi::isDone),
        nights = (0 until NIGHTS_AHEAD).map { offset ->
            val night = today.plusDays(offset)
            val (booked, held) = takenOn(night, active)
            NightUi(date = night, booked = booked, held = held)
        },
        decisions = holds
            .sortedBy { minOf(it.holdUntil ?: it.checkIn, it.checkIn) }
            .map { hold ->
                DecisionUi(
                    booking = hold,
                    houses = if (hold.isWholeResort) {
                        HousesText.WholeResort(houses.size)
                    } else {
                        HousesText.Names(hold.houseIds.mapNotNull { housesById[it]?.name })
                    },
                    headline = headlineFor(hold),
                )
            },
        months = months.map { month -> MonthUi(month, bookedFraction(month, active)) },
        wholeResort = active
            .filter { it.isWholeResort && it.checkOut.isAfter(today) && it.checkIn.isBefore(monthsEnd) }
            .minByOrNull(Booking::checkIn)
            ?.let { WholeResortNote(it.guestName, it.checkIn, it.checkOut, isHold = it.isOnHold) },
    )
}

/** Houses booked and held on [night]; a house counts once, as booked if any confirmed stay has it. */
private fun BookingLog.takenOn(night: LocalDate, active: List<Booking>): Pair<Int, Int> {
    val staying = active.filter { it.coversNight(night) }
    fun houseIdsOf(bookings: List<Booking>): Set<String> =
        if (bookings.any(Booking::isWholeResort)) houses.map(House::id).toSet() else bookings.flatMap(Booking::houseIds).toSet()
    val booked = houseIdsOf(staying.filterNot(Booking::isOnHold))
    val held = houseIdsOf(staying.filter(Booking::isOnHold)) - booked
    return booked.size to held.size
}

private fun BookingLog.headlineFor(hold: Booking): DecisionHeadline {
    val until = hold.holdUntil
    return when {
        !hold.checkIn.isAfter(today) -> DecisionHeadline.InHouse
        until != null && until.isBefore(hold.checkIn) -> DecisionHeadline.Releases(until)
        else -> DecisionHeadline.ArrivesIn(ChronoUnit.DAYS.between(today, hold.checkIn).toInt())
    }
}

/** House-nights inside [month] taken by [active] stays, out of every house every night. */
private fun BookingLog.bookedFraction(month: YearMonth, active: List<Booking>): Float {
    val start = month.atDay(1)
    val end = month.atEndOfMonth().plusDays(1)
    val capacity = houses.size.toLong() * month.lengthOfMonth()
    if (capacity == 0L) return 0f
    val taken = active.filter { it.overlaps(start, end) }.sumOf { booking ->
        val nights = ChronoUnit.DAYS.between(maxOf(booking.checkIn, start), minOf(booking.checkOut, end))
        nights * if (booking.isWholeResort) houses.size else booking.houseIds.size
    }
    return (taken.toFloat() / capacity).coerceAtMost(1f)
}

private fun Booking.toMove(housesById: Map<String, House>, isArrival: Boolean) = MovementUi(
    bookingId = id,
    guestName = guestName,
    houseNames = houseIds.mapNotNull { housesById[it]?.name },
    isWholeResort = isWholeResort,
    nights = nights,
    isArrival = isArrival,
    isDone = if (isArrival) checkedIn else checkedOut,
)
