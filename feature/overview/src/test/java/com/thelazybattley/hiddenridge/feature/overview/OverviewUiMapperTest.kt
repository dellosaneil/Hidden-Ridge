package com.thelazybattley.hiddenridge.feature.overview

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.usecase.BookingLog
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class OverviewUiMapperTest {

    private val today = LocalDate.of(2026, 10, 12)
    private val houses = listOf(
        House("c1", "Cabin 1", HouseType.CABIN),
        House("c2", "Cabin 2", HouseType.CABIN),
        House("ca", "Casita A", HouseType.CASITA),
        House("cb", "Casita B", HouseType.CASITA),
    )

    @Test
    fun `today's arrivals and departures, and only unmarked ones need doing`() {
        val ui = overview(
            stay("in", listOf("c1"), from = 0, to = 3),
            stay("done", listOf("c2"), from = 0, to = 2).copy(checkedIn = true),
            stay("out", listOf("ca"), from = -3, to = 0),
        )

        assertEquals(listOf("in", "done"), ui.arrivals.map(MovementUi::bookingId))
        assertEquals(listOf("out"), ui.departures.map(MovementUi::bookingId))
        assertEquals(listOf("in", "out"), ui.pendingMoves.map(MovementUi::bookingId))
        assertEquals(2, ui.occupiedTonight)
        assertEquals(2, ui.freeTonight)
    }

    @Test
    fun `each night counts booked and held houses once`() {
        val ui = overview(
            stay("booked", listOf("c1", "c2"), from = 0, to = 2),
            stay("hold", listOf("ca"), from = 1, to = 3, status = BookingStatus.HOLD),
            stay("cancelled", listOf("cb"), from = 0, to = 7, status = BookingStatus.CANCELLED),
        )

        assertEquals(7, ui.nights.size)
        assertEquals(listOf(2 to 0, 2 to 1, 0 to 1, 0 to 0), ui.nights.take(4).map { it.booked to it.held })
    }

    @Test
    fun `holds are ordered by what comes first, their release or the arrival`() {
        val ui = overview(
            stay("open", listOf("c1"), from = 2, to = 5, status = BookingStatus.HOLD),
            stay("timed", listOf("c2"), from = 33, to = 36, status = BookingStatus.HOLD).copy(holdUntil = today.plusDays(1)),
            stay("later", listOf("ca"), from = 20, to = 22, status = BookingStatus.HOLD).copy(holdUntil = today.plusDays(10)),
        )

        assertEquals(listOf("timed", "open", "later"), ui.decisions.map { it.booking.id })
        assertEquals(
            listOf(
                DecisionHeadline.Releases(today.plusDays(1)),
                DecisionHeadline.ArrivesIn(2),
                DecisionHeadline.Releases(today.plusDays(10)),
            ),
            ui.decisions.map(DecisionUi::headline),
        )
        assertEquals(HoldSummary(count = 3, releasingSoon = 1, noLimit = 1, later = 1), ui.holds)
    }

    @Test
    fun `months show the share of house-nights taken and the next whole resort block`() {
        val ui = overview(
            // 2 houses × 31 nights of October's 4 × 31.
            stay("half", listOf("c1", "c2"), from = -11, to = 20),
            stay("wedding", emptyList(), from = 67, to = 70, mode = BookingMode.WHOLE_RESORT),
        )

        assertEquals(listOf(YearMonth.of(2026, 10), YearMonth.of(2026, 11), YearMonth.of(2026, 12)), ui.months.map(MonthUi::month))
        assertEquals(0.5f, ui.months.first().bookedFraction, 0.001f)
        assertEquals("Guest wedding", ui.wholeResort?.guestName)
    }

    private fun overview(vararg bookings: Booking) = BookingLog(today, houses, bookings.toList()).toOverview()

    private fun stay(
        id: String,
        houseIds: List<String>,
        from: Long,
        to: Long,
        status: BookingStatus = BookingStatus.CONFIRMED,
        mode: BookingMode = BookingMode.INDIVIDUAL,
    ) = Booking(
        id = id,
        guestName = "Guest $id",
        houseIds = houseIds,
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
