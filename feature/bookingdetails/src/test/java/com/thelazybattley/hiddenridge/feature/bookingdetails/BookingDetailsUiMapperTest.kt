package com.thelazybattley.hiddenridge.feature.bookingdetails

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingDetails
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class BookingDetailsUiMapperTest {

    private val today = LocalDate.of(2026, 10, 12)
    private val cabin = House("c1", "Cabin 1", HouseType.CABIN)
    private val casita = House("ca", "Casita A", HouseType.CASITA)

    @Test
    fun `a single house shows its name and how many it sleeps`() {
        val ui = details(houses = listOf(cabin)).toUi()

        assertEquals(BookingKind.INDIVIDUAL, ui.kind)
        assertEquals(HousesUi.Single(name = "Cabin 1", sleeps = 2), ui.houses)
    }

    @Test
    fun `a group booking lists every house and the total it sleeps`() {
        val ui = details(houses = listOf(cabin, casita)).toUi()

        assertEquals(BookingKind.GROUP, ui.kind)
        assertEquals(HousesUi.Several(names = listOf("Cabin 1", "Casita A"), totalSleeps = 6), ui.houses)
    }

    @Test
    fun `a whole resort hold keeps its release date`() {
        val holdUntil = today.plusDays(3)
        val ui = details(
            houses = listOf(cabin, casita),
            mode = BookingMode.WHOLE_RESORT,
            status = BookingStatus.HOLD,
            holdUntil = holdUntil,
        ).toUi()

        assertEquals(BookingKind.WHOLE_RESORT, ui.kind)
        assertEquals(HousesUi.WholeResort(houseCount = 2, totalSleeps = 6), ui.houses)
        assertEquals(BookingStatusUi.OnHold(until = holdUntil), ui.status)
    }

    private fun details(
        houses: List<House>,
        mode: BookingMode = BookingMode.INDIVIDUAL,
        status: BookingStatus = BookingStatus.CONFIRMED,
        holdUntil: LocalDate? = null,
    ) = BookingDetails(
        booking = Booking(
            id = "b1",
            guestName = "Guest",
            houseIds = if (mode == BookingMode.WHOLE_RESORT) emptyList() else houses.map(House::id),
            checkIn = today,
            checkOut = today.plusDays(3),
            mode = mode,
            status = status,
            holdUntil = holdUntil,
            guestCount = 2,
            phone = "",
            note = "",
        ),
        houses = houses,
        today = today,
    )
}
