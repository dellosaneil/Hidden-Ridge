package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HouseFilterTest {

    private val today = LocalDate.of(2026, 10, 12)
    private val cabin = House("c1", "Cabin 1", HouseType.CABIN)
    private val casita = House("ca", "Casita A", HouseType.CASITA)

    @Test
    fun `a stay fits when any matching house is free`() {
        val availability = availability(stay(listOf("c1"), from = 0, to = 3))

        assertTrue(availability.fits(HouseFilter.AnyHouse, day(1), day(2)))
        assertFalse(availability.fits(HouseFilter.OfType(HouseType.CABIN), day(1), day(2)))
        assertFalse(availability.fits(HouseFilter.WholeResort, day(1), day(2)))
    }

    @Test
    fun `the next opening skips nights that are taken`() {
        val availability = availability(stay(listOf("c1"), from = 3, to = 6))

        assertEquals(day(6), availability.nextOpening(HouseFilter.OfType(HouseType.CABIN), from = day(4), nights = 2))
        assertEquals(day(1), availability.nextOpening(HouseFilter.OfType(HouseType.CABIN), from = day(1), nights = 2))
    }

    @Test
    fun `there is no next opening past the bookable window`() {
        val availability = availability(stay(listOf("c1"), from = 0, to = 30))

        assertNull(availability.nextOpening(HouseFilter.OfType(HouseType.CABIN), from = day(0), nights = 2))
    }

    @Test
    fun `a whole resort booking takes every house on its nights`() {
        val availability = availability(stay(emptyList(), from = 2, to = 4, mode = BookingMode.WHOLE_RESORT))

        val occupancy = availability.occupancyOn(day(3))

        assertEquals(setOf("c1", "ca"), occupancy.takenHouseIds)
        assertEquals("b", occupancy.wholeResortBooking?.id)
        assertTrue(availability.occupancyOn(day(4)).takenHouseIds.isEmpty())
    }

    private fun day(offset: Long) = today.plusDays(offset)

    private fun availability(vararg bookings: Booking) = Availability(
        today = today,
        window = DateWindow(today, today.plusDays(30)),
        houses = listOf(cabin, casita),
        bookings = bookings.toList(),
    )

    private fun stay(houseIds: List<String>, from: Long, to: Long, mode: BookingMode = BookingMode.INDIVIDUAL) = Booking(
        id = "b",
        guestName = "Guest",
        houseIds = houseIds,
        checkIn = day(from),
        checkOut = day(to),
        mode = mode,
        status = BookingStatus.CONFIRMED,
        holdUntil = null,
        guestCount = 2,
        phone = "",
        note = "",
    )
}
