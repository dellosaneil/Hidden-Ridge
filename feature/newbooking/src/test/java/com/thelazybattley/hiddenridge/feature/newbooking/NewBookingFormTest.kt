package com.thelazybattley.hiddenridge.feature.newbooking

import com.thelazybattley.hiddenridge.core.domain.model.Availability
import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingDraft
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NewBookingFormTest {

    private val today = LocalDate.of(2026, 10, 12)
    private val cabin1 = House("c1", "Cabin 1", HouseType.CABIN)
    private val cabin2 = House("c2", "Cabin 2", HouseType.CABIN)
    private val casitaA = House("ca", "Casita A", HouseType.CASITA)
    private val ridge = House("f1", "Ridge House", HouseType.FAMILY_HOUSE)
    private val herrera = Booking(
        id = "b1",
        guestName = "Herrera",
        houseIds = listOf("ca"),
        checkIn = today.plusDays(2),
        checkOut = today.plusDays(5),
        mode = BookingMode.INDIVIDUAL,
        status = BookingStatus.CONFIRMED,
        holdUntil = null,
        guestCount = 4,
        phone = "",
        note = "",
    )
    private val availability = Availability(
        today = today,
        window = DateWindow(today, today.plusDays(400)),
        houses = listOf(cabin1, cabin2, casitaA, ridge),
        bookings = listOf(herrera),
    )

    @Test
    fun `editing a booking starts from its own fields and never clashes with itself`() {
        val ui = NewBookingForm.editing(herrera, FormPurpose.EDIT, today).toUi(availability)

        assertEquals("Herrera", ui.draft.guestName)
        assertEquals(listOf("ca"), ui.draft.houseIds)
        assertTrue(ui.conflicts.isEmpty())
        assertEquals(HouseTileState.SELECTED, ui.houses.single { it.houseId == "ca" }.state)
    }

    @Test
    fun `an in-house stay keeps its past check-in when edited`() {
        val inHouse = herrera.copy(checkIn = today.minusDays(1))

        val ui = NewBookingForm.editing(inHouse, FormPurpose.EDIT, today).toUi(availability)

        assertTrue(ui.problems.isEmpty())
    }

    @Test
    fun `a house tile shows who holds it for the chosen dates`() {
        val form = NewBookingForm(
            draft = BookingDraft(checkIn = today.plusDays(3), checkOut = today.plusDays(4)),
            houseTypeTab = HouseType.CASITA,
        )

        val tile = form.toUi(availability).houses.single()

        assertEquals(HouseTileState.TAKEN, tile.state)
        assertEquals("Herrera", tile.takenBy?.guestName)
    }

    @Test
    fun `picking a taken house surfaces the conflict`() {
        val form = NewBookingForm(
            draft = BookingDraft(houseIds = listOf("ca"), checkIn = today.plusDays(3), checkOut = today.plusDays(4)),
        )

        val ui = form.toUi(availability)

        assertEquals(listOf("Herrera"), ui.conflicts.map(ConflictUi::guestName))
        assertEquals(HouseType.CASITA, ui.houseTypes.single { it.isActive }.type)
    }

    @Test
    fun `the calendar bands the stay and strikes nights the chosen house is taken`() {
        val form = NewBookingForm(
            draft = BookingDraft(houseIds = listOf("ca"), checkIn = today.plusDays(6), checkOut = today.plusDays(8)),
        )

        val days = form.toUi(availability).calendar.days.associateBy(CalendarDayUi::date)

        assertEquals(RangeBand.START_HALF, days.getValue(today.plusDays(6)).band)
        assertEquals(RangeBand.FULL, days.getValue(today.plusDays(7)).band)
        assertEquals(RangeBand.END_HALF, days.getValue(today.plusDays(8)).band)
        assertTrue(days.getValue(today.plusDays(2)).isBlocked)
        assertTrue(days.getValue(today.minusDays(1)).isPast)
    }

    @Test
    fun `the calendar opens on today's month and stays inside the window`() {
        val ui = NewBookingForm(draft = BookingDraft(), visibleMonth = YearMonth.of(2020, 1)).toUi(availability)

        assertEquals(YearMonth.of(2026, 10), ui.calendar.month)
        assertNull(ui.holdUntil)
    }

    @Test
    fun `short labels match the tiles in the design`() {
        assertEquals("1", cabin1.shortLabel())
        assertEquals("A", casitaA.shortLabel())
        assertEquals("Ridge", ridge.shortLabel())
    }
}
