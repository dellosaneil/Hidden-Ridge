package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingMoveTest {

    private val today = LocalDate.of(2026, 10, 12)
    private val until = day(60)

    @Test
    fun `a stay with nights left can move, a finished or cancelled one can't`() {
        assertTrue(stay("b1", listOf("c1"), from = -2, to = 1).canMoveOn(today))
        assertTrue(stay("w1", emptyList(), from = 2, to = 4, mode = BookingMode.WHOLE_RESORT).canMoveOn(today))
        assertFalse(stay("b1", listOf("c1"), from = -2, to = 0).canMoveOn(today))
        assertFalse(stay("b1", listOf("c1"), from = 2, to = 4, status = BookingStatus.CANCELLED).canMoveOn(today))
    }

    @Test
    fun `only a single-house booking can change house`() {
        assertTrue(stay("b1", listOf("c1"), from = 2, to = 4).canChangeHouse)
        assertFalse(stay("b1", listOf("c1", "c2"), from = 2, to = 4).canChangeHouse)
        assertFalse(stay("w1", emptyList(), from = 2, to = 4, mode = BookingMode.WHOLE_RESORT).canChangeHouse)
    }

    @Test
    fun `a future stay can come forward to today, an in-house one only later`() {
        assertEquals(-3..56, stay("b1", listOf("c1"), from = 3, to = 4).allowedShift(today, until))
        assertEquals(0..59, stay("b1", listOf("c1"), from = -2, to = 1).allowedShift(today, until))
    }

    @Test
    fun `moving keeps the length of stay and pulls a hold release back to check-in`() {
        val hold = stay("b1", listOf("c1"), from = 5, to = 8).copy(status = BookingStatus.HOLD, holdUntil = day(4))

        val moved = hold.movedTo(day(2), listOf("c2"))

        assertEquals(day(5), moved.checkOut)
        assertEquals(listOf("c2"), moved.houseIds)
        assertEquals(day(2), moved.holdUntil)
    }

    @Test
    fun `problems are reported before anything is saved`() {
        val single = stay("b1", listOf("c1"), from = 2, to = 4)
        val group = stay("g1", listOf("c1", "c2"), from = 2, to = 4)

        assertEquals(MoveProblem.UNCHANGED, single.moveProblem(day(2), listOf("c1"), today, until))
        assertEquals(MoveProblem.OUT_OF_RANGE, single.moveProblem(day(-1), listOf("c1"), today, until))
        assertEquals(MoveProblem.HOUSE_LOCKED, group.moveProblem(day(2), listOf("c3", "c2"), today, until))
        assertNull(group.moveProblem(day(5), listOf("c2", "c1"), today, until))
        assertNull(single.moveProblem(day(2), listOf("c3"), today, until))
    }

    @Test
    fun `the booking being moved never conflicts with itself`() {
        val booking = stay("b1", listOf("c1"), from = 2, to = 4)

        assertTrue(listOf(booking).conflictsForMove(booking.movedTo(day(3), listOf("c1"))).isEmpty())
    }

    @Test
    fun `moving onto another guest's nights conflicts`() {
        val booking = stay("b1", listOf("c1"), from = 2, to = 4)
        val other = stay("b2", listOf("c2"), from = 5, to = 7)

        val conflicts = listOf(booking, other).conflictsForMove(booking.movedTo(day(4), listOf("c2")))

        assertEquals(listOf("b2"), conflicts.map { it.booking.id })
    }

    @Test
    fun `a whole resort block conflicts with any house it lands on`() {
        val wedding = stay("w1", emptyList(), from = 2, to = 4, mode = BookingMode.WHOLE_RESORT)
        val other = stay("b2", listOf("c3"), from = 5, to = 6)

        val conflicts = listOf(wedding, other).conflictsForMove(wedding.movedTo(day(4), emptyList()))

        assertEquals(listOf("b2"), conflicts.map { it.booking.id })
    }

    private fun day(offset: Long): LocalDate = today.plusDays(offset)

    private fun stay(
        id: String,
        houseIds: List<String>,
        from: Long,
        to: Long,
        mode: BookingMode = BookingMode.INDIVIDUAL,
        status: BookingStatus = BookingStatus.CONFIRMED,
    ) = Booking(
        id = id,
        guestName = id,
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
