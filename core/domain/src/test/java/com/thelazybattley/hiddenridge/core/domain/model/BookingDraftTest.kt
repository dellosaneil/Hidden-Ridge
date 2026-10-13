package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingDraftTest {

    private val today = LocalDate.of(2026, 10, 12)

    @Test
    fun `an in-house booking can keep its past check-in, but not move it to another past day`() {
        val draft = booking(checkIn = today.minusDays(2), checkOut = today.plusDays(2)).toDraft(today)

        assertTrue(draft.problems(today, keptCheckIn = today.minusDays(2)).isEmpty())
        assertEquals(
            listOf(DraftProblem.CHECK_IN_IN_PAST),
            draft.copy(checkIn = today.minusDays(3)).problems(today, keptCheckIn = today.minusDays(2)),
        )
        assertEquals(listOf(DraftProblem.CHECK_IN_IN_PAST), draft.problems(today))
    }

    @Test
    fun `a booking becomes a draft with every field it had`() {
        val booking = booking(checkIn = today.plusDays(3), checkOut = today.plusDays(5))

        val draft = booking.toDraft(today)

        assertEquals(BookingMode.INDIVIDUAL, draft.mode)
        assertEquals(listOf("c1", "c2"), draft.houseIds)
        assertEquals("Kim yoga group", draft.guestName)
        assertEquals("(555) 100-2000", draft.phone)
        assertEquals(6, draft.guestCount)
        assertEquals("Late arrival", draft.note)
        assertEquals(BookingStatus.CONFIRMED, draft.status)
    }

    @Test
    fun `a hold keeps its release date`() {
        val hold = booking(checkIn = today.plusDays(10), checkOut = today.plusDays(12))
            .copy(status = BookingStatus.HOLD, holdUntil = today.plusDays(4))

        val draft = hold.toDraft(today)

        assertEquals(BookingStatus.HOLD, draft.status)
        assertEquals(today.plusDays(4), draft.holdUntil(today))
    }

    private fun booking(checkIn: LocalDate, checkOut: LocalDate) = Booking(
        id = "b1",
        guestName = "Kim yoga group",
        houseIds = listOf("c1", "c2"),
        checkIn = checkIn,
        checkOut = checkOut,
        mode = BookingMode.INDIVIDUAL,
        status = BookingStatus.CONFIRMED,
        holdUntil = null,
        guestCount = 6,
        phone = "(555) 100-2000",
        note = "Late arrival",
    )
}
