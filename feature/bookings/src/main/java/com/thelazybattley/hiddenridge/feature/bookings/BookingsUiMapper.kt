package com.thelazybattley.hiddenridge.feature.bookings

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.usecase.BookingLog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

private const val DAYS_PER_WEEK = 7L

/** Today's guests in the order front desk deals with them: arrivals, departures, then those staying. */
private val TodayOrder = listOf(RowStatus.ARRIVING, RowStatus.LEAVING, RowStatus.CHECKED_OUT, RowStatus.IN_HOUSE)

internal fun BookingLog.toUi(tab: BookingsTab, filter: BookingsFilter, firstDayOfWeek: DayOfWeek): BookingsUi {
    val housesById = houses.associateBy(House::id)
    val weekEnd = today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek)).plusDays(DAYS_PER_WEEK)
    val (cancelled, active) = bookings.partition { it.status == BookingStatus.CANCELLED }
    val here = active.filter { !it.checkIn.isAfter(today) && !it.checkOut.isBefore(today) }
    val ahead = active.filter { it.checkIn.isAfter(today) }.sortedBy(Booking::checkIn)
    val past = (active.filter { it.checkOut.isBefore(today) } + cancelled).sortedByDescending(Booking::checkOut)

    val upcoming: List<Pair<GroupLabel, BookingRowUi>> =
        here.map { GroupLabel.Today(today) to it.toRow(housesById, todayStatus(it), isToday = true, showWeekdays = false) }
            .sortedBy { (_, row) -> TodayOrder.indexOf(row.status) } +
            ahead.map { booking ->
                val thisWeek = booking.checkIn.isBefore(weekEnd)
                val label = if (thisWeek) GroupLabel.ThisWeek else monthLabel(booking.checkIn)
                label to booking.toRow(housesById, aheadStatus(booking), isToday = false, showWeekdays = thisWeek)
            }
    val history: List<Pair<GroupLabel, BookingRowUi>> = past.map { booking ->
        val status = if (booking.status == BookingStatus.CANCELLED) RowStatus.CANCELLED else RowStatus.CHECKED_OUT
        pastLabel(booking) to booking.toRow(housesById, status, isToday = false, showWeekdays = false)
    }
    val shown = if (tab == BookingsTab.UPCOMING) upcoming else history

    return BookingsUi(
        today = today,
        tab = tab,
        filter = filter,
        tabCounts = mapOf(BookingsTab.UPCOMING to upcoming.size, BookingsTab.PAST to history.size),
        filterCounts = BookingsFilter.entries.associateWith { f -> shown.count { (_, row) -> row.matches(f) } },
        rows = shown.filter { (_, row) -> row.matches(filter) }.grouped(),
    )
}

private fun BookingLog.monthLabel(date: LocalDate) = GroupLabel.Month(YearMonth.from(date), showYear = date.year != today.year)

/** Stays that ended this month read "Earlier in October"; anything else, such as a cancelled stay still ahead, its month. */
private fun BookingLog.pastLabel(booking: Booking): GroupLabel {
    val month = YearMonth.from(booking.checkOut)
    return if (month == YearMonth.from(today) && booking.checkOut.isBefore(today)) GroupLabel.EarlierIn(month) else monthLabel(booking.checkOut)
}

private fun BookingRowUi.matches(filter: BookingsFilter) = filter == BookingsFilter.ALL || kind == filter

/** A header before each run of rows with the same label; labels arrive already in order. */
private fun List<Pair<GroupLabel, BookingRowUi>>.grouped(): List<BookingsRow> {
    val groups = LinkedHashMap<GroupLabel, MutableList<BookingRowUi>>()
    forEach { (label, row) -> groups.getOrPut(label) { mutableListOf() } += row }
    return groups.flatMap { (label, rows) ->
        listOf(BookingsRow.Group(label, rows.size)) +
            rows.mapIndexed { index, row -> BookingsRow.Booking(row, isLastInGroup = index == rows.lastIndex) }
    }
}

private fun BookingLog.todayStatus(booking: Booking): RowStatus = when {
    booking.checkOut == today -> if (booking.checkedOut) RowStatus.CHECKED_OUT else RowStatus.LEAVING
    booking.checkIn == today && !booking.checkedIn -> RowStatus.ARRIVING
    else -> RowStatus.IN_HOUSE
}

private fun aheadStatus(booking: Booking): RowStatus = when {
    booking.isOnHold -> RowStatus.ON_HOLD
    booking.isWholeResort -> RowStatus.WHOLE_RESORT
    else -> RowStatus.CONFIRMED
}

private fun Booking.toRow(
    housesById: Map<String, House>,
    status: RowStatus,
    isToday: Boolean,
    showWeekdays: Boolean,
) = BookingRowUi(
    bookingId = id,
    guestName = guestName,
    checkIn = checkIn,
    checkOut = checkOut,
    nights = nights,
    houses = if (isWholeResort) {
        HousesLabel.WholeResort(housesById.size)
    } else {
        HousesLabel.Names(houseIds.mapNotNull { housesById[it]?.name })
    },
    status = status,
    kind = when {
        isWholeResort -> BookingsFilter.WHOLE_RESORT
        isOnHold -> BookingsFilter.HOLD
        else -> BookingsFilter.CONFIRMED
    },
    isToday = isToday,
    showWeekdays = showWeekdays,
    holdUntil = holdUntil.takeIf { isOnHold && status == RowStatus.ON_HOLD },
)
