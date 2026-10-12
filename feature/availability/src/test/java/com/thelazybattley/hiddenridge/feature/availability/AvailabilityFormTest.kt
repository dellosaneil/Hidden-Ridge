package com.thelazybattley.hiddenridge.feature.availability

import com.thelazybattley.hiddenridge.core.domain.model.Availability
import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseFilter
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AvailabilityFormTest {

    private val today = LocalDate.of(2026, 10, 12)
    private val availability = Availability(
        today = today,
        window = DateWindow(today, today.plusDays(300)),
        houses = listOf(
            House("c1", "Cabin 1", HouseType.CABIN),
            House("c2", "Cabin 2", HouseType.CABIN),
            House("ca", "Casita A", HouseType.CASITA),
        ),
        bookings = listOf(
            booking("b1", "Nguyen", listOf("c1"), from = 2, to = 4),
            booking("w1", "Garcia reunion", emptyList(), from = 10, to = 14, mode = BookingMode.WHOLE_RESORT, status = BookingStatus.HOLD),
        ),
    )

    @Test
    fun `two taps choose a stay and a third starts over`() {
        val form = AvailabilityForm()
            .picking(day(5))
            .picking(day(8))

        assertEquals(day(5), form.checkIn)
        assertEquals(3, form.nights)

        val restarted = form.picking(day(6))
        assertEquals(day(6), restarted.checkIn)
        assertNull(restarted.checkOut)
    }

    @Test
    fun `nights show free houses among those the filter covers`() {
        val ui = AvailabilityForm(filter = HouseFilter.OfType(HouseType.CABIN)).toUi(availability)

        val night = ui.calendar.days.single { it.date == day(3) }
        assertEquals(1, night.freeCount)
        assertEquals(2, night.total)
        assertEquals(DayTone.SOME_FREE, night.tone)
        assertEquals(DayTone.RESORT_HOLD, ui.calendar.days.single { it.date == day(11) }.tone)
        assertEquals(DayTone.PAST, ui.calendar.days.single { it.date == today.minusDays(1) }.tone)
    }

    @Test
    fun `a stay lists each house with whoever holds it`() {
        val result = AvailabilityForm(checkIn = day(2), checkOut = day(5)).toUi(availability).result as AvailabilityResultUi.Houses

        assertEquals(listOf("Nguyen", null, null), result.rows.map { it.takenBy?.guestName })
        assertTrue(result.fits)
        assertNull(result.nextOpening)
    }

    @Test
    fun `a blocked whole resort stay offers the next opening`() {
        val form = AvailabilityForm(filter = HouseFilter.WholeResort, checkIn = day(11), checkOut = day(13))

        val result = form.toUi(availability).result as AvailabilityResultUi.WholeResort

        assertFalse(result.fits)
        assertEquals(listOf("Garcia reunion"), result.conflicts.map(ConflictRowUi::guestName))
        assertEquals(day(14), result.nextOpening)
        assertEquals(day(14), form.startingOn(day(14)).checkIn)
        assertEquals(day(16), form.startingOn(day(14)).checkOut)
    }

    private fun day(offset: Long) = today.plusDays(offset)

    private fun booking(
        id: String,
        guest: String,
        houseIds: List<String>,
        from: Long,
        to: Long,
        mode: BookingMode = BookingMode.INDIVIDUAL,
        status: BookingStatus = BookingStatus.CONFIRMED,
    ) = Booking(
        id = id,
        guestName = guest,
        houseIds = houseIds,
        checkIn = day(from),
        checkOut = day(to),
        mode = mode,
        status = status,
        holdUntil = null,
        guestCount = 2,
        phone = "",
        note = "",
    )
}
