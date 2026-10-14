package com.thelazybattley.hiddenridge.feature.bookings

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.usecase.BookingLog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class BookingsUiMapperTest {

    /** A Monday. */
    private val today = LocalDate.of(2026, 10, 12)
    private val houses = listOf(
        House("c1", "Cabin 1", HouseType.CABIN),
        House("c2", "Cabin 2", HouseType.CABIN),
        House("ca", "Casita A", HouseType.CASITA),
    )

    @Test
    fun `upcoming starts with today then this week then each month`() {
        val ui = ui(
            stay("dec", from = 60, to = 63),
            stay("staying", from = -2, to = 2),
            stay("week", from = 3, to = 5),
            stay("arriving", from = 0, to = 3),
            stay("leaving", from = -3, to = 0),
            stay("jan", from = 90, to = 92),
        )

        assertEquals(
            listOf(
                GroupLabel.Today(today), "arriving", "leaving", "staying",
                GroupLabel.ThisWeek, "week",
                GroupLabel.Month(YearMonth.of(2026, 12), showYear = false), "dec",
                GroupLabel.Month(YearMonth.of(2027, 1), showYear = true), "jan",
            ),
            ui.flat(),
        )
        assertEquals(
            listOf(RowStatus.ARRIVING, RowStatus.LEAVING, RowStatus.IN_HOUSE, RowStatus.CONFIRMED, RowStatus.CONFIRMED, RowStatus.CONFIRMED),
            ui.bookingRows().map(BookingRowUi::status),
        )
        assertEquals(listOf(false, false, false, true, false, false), ui.bookingRows().map(BookingRowUi::showWeekdays))
    }

    @Test
    fun `checked in arrivals are in-house and checked out departures say so`() {
        val ui = ui(
            stay("arrived", from = 0, to = 2).copy(checkedIn = true),
            stay("gone", from = -2, to = 0).copy(checkedOut = true),
        )

        assertEquals(listOf(RowStatus.CHECKED_OUT, RowStatus.IN_HOUSE), ui.bookingRows().map(BookingRowUi::status))
    }

    @Test
    fun `past holds finished stays and every cancelled booking, latest first`() {
        val ui = ui(
            stay("earlier", from = -6, to = -3),
            stay("september", from = -30, to = -25),
            stay("cancelled", from = 20, to = 22, status = BookingStatus.CANCELLED),
            stay("upcoming", from = 3, to = 4),
            tab = BookingsTab.PAST,
        )

        assertEquals(
            listOf(
                GroupLabel.Month(YearMonth.of(2026, 11), showYear = false), "cancelled",
                GroupLabel.EarlierIn(YearMonth.of(2026, 10)), "earlier",
                GroupLabel.Month(YearMonth.of(2026, 9), showYear = false), "september",
            ),
            ui.flat(),
        )
        assertEquals(listOf(RowStatus.CANCELLED, RowStatus.CHECKED_OUT, RowStatus.CHECKED_OUT), ui.bookingRows().map(BookingRowUi::status))
        assertEquals(mapOf(BookingsTab.UPCOMING to 1, BookingsTab.PAST to 3), ui.tabCounts)
    }

    @Test
    fun `chips filter by kind and count within the tab`() {
        val bookings = arrayOf(
            stay("confirmed", from = 3, to = 4),
            stay("hold", from = 5, to = 6, status = BookingStatus.HOLD),
            stay("resort", from = 30, to = 32, mode = BookingMode.WHOLE_RESORT),
        )

        val holds = ui(*bookings, filter = BookingsFilter.HOLD)
        assertEquals(listOf("hold"), holds.bookingRows().map(BookingRowUi::bookingId))
        assertEquals(
            mapOf(BookingsFilter.ALL to 3, BookingsFilter.CONFIRMED to 1, BookingsFilter.HOLD to 1, BookingsFilter.WHOLE_RESORT to 1),
            holds.filterCounts,
        )
        val resort = ui(*bookings).bookingRows().single { it.bookingId == "resort" }
        assertEquals(RowStatus.WHOLE_RESORT, resort.status)
        assertEquals(HousesLabel.WholeResort(3), resort.houses)
    }

    private fun ui(
        vararg bookings: Booking,
        tab: BookingsTab = BookingsTab.UPCOMING,
        filter: BookingsFilter = BookingsFilter.ALL,
    ) = BookingLog(today, houses, bookings.toList()).toUi(tab, filter, DayOfWeek.SUNDAY)

    private fun BookingsUi.bookingRows() = rows.filterIsInstance<BookingsRow.Booking>().map(BookingsRow.Booking::booking)

    private fun BookingsUi.flat(): List<Any> = rows.map { row ->
        when (row) {
            is BookingsRow.Group -> row.label
            is BookingsRow.Booking -> row.booking.bookingId
        }
    }

    private fun stay(
        id: String,
        from: Long,
        to: Long,
        status: BookingStatus = BookingStatus.CONFIRMED,
        mode: BookingMode = BookingMode.INDIVIDUAL,
    ) = Booking(
        id = id,
        guestName = "Guest $id",
        houseIds = if (mode == BookingMode.WHOLE_RESORT) emptyList() else listOf("c1"),
        checkIn = today.plusDays(from),
        checkOut = today.plusDays(to),
        mode = mode,
        status = status,
        holdUntil = null,
        guestCount = 2,
        phone = "",
        note = "",
    )
}
