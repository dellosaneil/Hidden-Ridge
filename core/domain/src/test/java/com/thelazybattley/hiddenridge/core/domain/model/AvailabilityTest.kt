package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AvailabilityTest {

    private val today = LocalDate.of(2026, 10, 12)
    private val houses = listOf(
        House("c1", "Cabin 1", HouseType.CABIN),
        House("c2", "Cabin 2", HouseType.CABIN),
    )

    @Test
    fun `back-to-back stays in the same house don't clash`() {
        val availability = availability(stay("b1", listOf("c1"), from = 0, to = 3))

        assertTrue(availability.isFree("c1", day(3), day(5)))
        assertFalse(availability.isFree("c1", day(2), day(5)))
    }

    @Test
    fun `a whole resort request clashes with any overlapping booking`() {
        val availability = availability(stay("b1", listOf("c2"), from = 1, to = 2))

        val conflicts = availability.conflictsFor(StayRequest(BookingMode.WHOLE_RESORT, emptyList(), day(0), day(3)))

        assertEquals(listOf("b1"), conflicts.map { it.booking.id })
        assertEquals(listOf("c2"), conflicts.single().houseIds)
    }

    @Test
    fun `a whole resort booking blocks every house`() {
        val availability = availability(stay("w1", emptyList(), from = 5, to = 7, mode = BookingMode.WHOLE_RESORT))

        assertFalse(availability.isFree("c1", day(6), day(8)))
        assertFalse(availability.isFree("c2", day(4), day(6)))
    }

    @Test
    fun `with no house chosen, only fully booked nights are blocked`() {
        val availability = availability(
            stay("b1", listOf("c1"), from = 0, to = 3),
            stay("b2", listOf("c2"), from = 2, to = 4),
        )

        val blocked = availability.blockedNights(BookingMode.INDIVIDUAL, emptyList(), day(0), day(5))

        assertEquals(setOf(day(2)), blocked)
    }

    @Test
    fun `with a house chosen, its taken nights are blocked`() {
        val availability = availability(stay("b1", listOf("c1"), from = 0, to = 3))

        val blocked = availability.blockedNights(BookingMode.INDIVIDUAL, listOf("c1"), day(0), day(5))

        assertEquals(setOf(day(0), day(1), day(2)), blocked)
    }

    @Test
    fun `a draft lists what is still missing in form order`() {
        val problems = BookingDraft(guestName = " ").problems(today)

        assertEquals(
            listOf(DraftProblem.MISSING_CHECK_IN, DraftProblem.NO_HOUSES, DraftProblem.MISSING_GUEST_NAME),
            problems,
        )
    }

    private fun day(offset: Long) = today.plusDays(offset)

    private fun availability(vararg bookings: Booking) = Availability(
        today = today,
        window = DateWindow(today, today.plusDays(60)),
        houses = houses,
        bookings = bookings.toList(),
    )

    private fun stay(
        id: String,
        houseIds: List<String>,
        from: Long,
        to: Long,
        mode: BookingMode = BookingMode.INDIVIDUAL,
    ) = Booking(
        id = id,
        guestName = "Guest $id",
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
