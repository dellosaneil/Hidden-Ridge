package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HouseRosterTest {

    private val today = LocalDate.of(2026, 10, 12)
    private val cabin = House("c1", "Cabin 1", HouseType.CABIN)
    private val casita = House("ca", "Casita A", HouseType.CASITA)

    @Test
    fun `a house turning over today has someone leaving and someone arriving`() {
        val roster = roster(
            stay("out", listOf("c1"), from = -3, to = 0),
            stay("in", listOf("c1"), from = 0, to = 2),
            stay("later", listOf("c1"), from = 5, to = 7),
        )

        val cabinDay = roster.houses.first()
        assertEquals("out", cabinDay.departing?.id)
        assertEquals("in", cabinDay.tonight?.id)
        assertEquals("later", cabinDay.next?.id)
    }

    @Test
    fun `the next stay is the earliest one after today`() {
        val roster = roster(
            stay("far", listOf("c1"), from = 10, to = 12),
            stay("soon", listOf("c1"), from = 3, to = 5),
        )

        val cabinDay = roster.houses.first()
        assertNull(cabinDay.tonight)
        assertNull(cabinDay.departing)
        assertEquals("soon", cabinDay.next?.id)
    }

    @Test
    fun `a whole resort stay is in every house`() {
        val roster = roster(stay("resort", emptyList(), from = -1, to = 2, mode = BookingMode.WHOLE_RESORT))

        assertEquals(listOf("resort", "resort"), roster.houses.map { it.tonight?.id })
    }

    @Test
    fun `the stay on a night is the one covering it`() {
        val cabinDay = roster(stay("hold", listOf("c1"), from = 2, to = 4)).houses.first()

        assertNull(cabinDay.stayOn(today.plusDays(1)))
        assertEquals("hold", cabinDay.stayOn(today.plusDays(3))?.id)
        assertNull(cabinDay.stayOn(today.plusDays(4)))
    }

    @Test
    fun `stays only count for the houses they book`() {
        val roster = roster(stay("cabin", listOf("c1"), from = 0, to = 2))

        assertEquals("cabin", roster.houses[0].tonight?.id)
        assertNull(roster.houses[1].tonight)
    }

    private fun roster(vararg bookings: Booking) = houseRoster(today, listOf(cabin, casita), bookings.toList())

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
        checkIn = today.plusDays(from),
        checkOut = today.plusDays(to),
        mode = mode,
        status = BookingStatus.CONFIRMED,
        holdUntil = null,
        guestCount = 2,
        phone = "",
        note = "",
    )
}
