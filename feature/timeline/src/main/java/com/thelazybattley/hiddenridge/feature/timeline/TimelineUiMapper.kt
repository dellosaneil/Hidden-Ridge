package com.thelazybattley.hiddenridge.feature.timeline

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.model.Timeline
import java.time.YearMonth
import java.time.temporal.ChronoUnit

internal fun Timeline.toUi(): TimelineUi {
    val rows = houseRows(houses)
    val bookingsByMode = bookings.partition(Booking::isWholeResort)
    return TimelineUi(
        today = today,
        todayIndex = window.indexOf(today),
        windowStart = window.start,
        dayCount = window.dayCount,
        houseCount = houses.size,
        months = window.months().map { it.withBookings(bookings, houses.size, window) },
        rows = rows,
        bookingBars = bookingsByMode.second.flatMap { booking -> booking.toBars(window, rows) },
        resortBlocks = bookingsByMode.first.map { booking -> booking.toResortBlock(window) },
    )
}

/** Houses grouped under a header per type, types in their natural order. */
private fun houseRows(houses: List<House>): List<TimelineRow> =
    HouseType.entries.flatMap { type ->
        val ofType = houses.filter { it.type == type }
        if (ofType.isEmpty()) {
            emptyList()
        } else {
            listOf(TimelineRow.Group(type)) + ofType.map { TimelineRow.House(houseId = it.id, name = it.name) }
        }
    }

private fun DateWindow.months(): List<TimelineMonth> =
    generateSequence(YearMonth.from(start)) { it.plusMonths(1) }
        .takeWhile { it.atDay(1).isBefore(endExclusive) }
        .map { month ->
            val first = maxOf(month.atDay(1), start)
            val last = minOf(month.atEndOfMonth().plusDays(1), endExclusive)
            TimelineMonth(yearMonth = month, startIndex = indexOf(first), dayCount = indexOf(last) - indexOf(first))
        }
        .toList()

/** How full [this] month is, counting nights each booking spends inside it. */
private fun TimelineMonth.withBookings(bookings: List<Booking>, houseCount: Int, window: DateWindow): TimelineMonth {
    val start = window.dateAt(startIndex)
    val end = window.dateAt(startIndex + dayCount)
    val inMonth = bookings.filter { it.overlaps(start, end) }
    val bookedNights = inMonth.sumOf { booking ->
        val nights = ChronoUnit.DAYS.between(maxOf(booking.checkIn, start), minOf(booking.checkOut, end))
        nights * if (booking.isWholeResort) houseCount else booking.houseIds.size
    }
    val capacity = houseCount.toLong() * dayCount
    return copy(
        bookedFraction = if (capacity == 0L) 0f else (bookedNights.toFloat() / capacity).coerceAtMost(1f),
        hasWholeResort = inMonth.any(Booking::isWholeResort),
    )
}

private fun Booking.toBars(window: DateWindow, rows: List<TimelineRow>): List<BookingBar> =
    houseIds.mapNotNull { houseId ->
        val rowIndex = rows.indexOfFirst { it is TimelineRow.House && it.houseId == houseId }
        val row = rows.getOrNull(rowIndex) as? TimelineRow.House ?: return@mapNotNull null
        BookingBar(
            bookingId = id,
            guestName = guestName,
            houseName = row.name,
            rowIndex = rowIndex,
            startIndex = window.indexOf(checkIn),
            nights = nights,
            checkIn = checkIn,
            checkOut = checkOut,
            isOnHold = isOnHold,
            isGroup = isGroup,
        )
    }

private fun Booking.toResortBlock(window: DateWindow) = ResortBlock(
    bookingId = id,
    guestName = guestName,
    startIndex = window.indexOf(checkIn),
    nights = nights,
    checkIn = checkIn,
    checkOut = checkOut,
    isOnHold = isOnHold,
    holdUntil = holdUntil,
)
