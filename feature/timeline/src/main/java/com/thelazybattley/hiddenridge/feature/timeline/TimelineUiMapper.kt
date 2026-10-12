package com.thelazybattley.hiddenridge.feature.timeline

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.model.Timeline
import java.time.YearMonth

internal fun Timeline.toUi(): TimelineUi {
    val rows = houseRows(houses)
    val bookingsByMode = bookings.partition(Booking::isWholeResort)
    return TimelineUi(
        today = today,
        todayIndex = window.indexOf(today),
        windowStart = window.start,
        dayCount = window.dayCount,
        houseCount = houses.size,
        months = window.months(),
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
