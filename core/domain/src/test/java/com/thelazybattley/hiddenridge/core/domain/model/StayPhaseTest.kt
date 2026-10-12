package com.thelazybattley.hiddenridge.core.domain.model

import com.thelazybattley.hiddenridge.core.domain.usecase.PutBookingOnHoldUseCase
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class StayPhaseTest {

    private val today = LocalDate.of(2026, 10, 12)

    @Test
    fun `stay phase follows check-in and check-out`() {
        assertEquals(StayPhase.Departed, stay(from = -5, to = -1).stayPhaseOn(today))
        assertEquals(StayPhase.LeavesToday, stay(from = -3, to = 0).stayPhaseOn(today))
        assertEquals(StayPhase.ArrivesToday, stay(from = 0, to = 3).stayPhaseOn(today))
        assertEquals(StayPhase.InHouse(night = 3, nights = 4), stay(from = -2, to = 2).stayPhaseOn(today))
        assertEquals(StayPhase.ArrivesIn(days = 1), stay(from = 1, to = 3).stayPhaseOn(today))
    }

    @Test
    fun `a new hold lasts three days but never past check-in`() {
        assertEquals(today.plusDays(3), PutBookingOnHoldUseCase.holdUntil(today, checkIn = today.plusDays(10)))
        assertEquals(today.plusDays(1), PutBookingOnHoldUseCase.holdUntil(today, checkIn = today.plusDays(1)))
        assertEquals(today, PutBookingOnHoldUseCase.holdUntil(today, checkIn = today.minusDays(1)))
    }

    private fun stay(from: Long, to: Long) = Booking(
        id = "b",
        guestName = "Guest",
        houseIds = listOf("c1"),
        checkIn = today.plusDays(from),
        checkOut = today.plusDays(to),
        mode = BookingMode.INDIVIDUAL,
        status = BookingStatus.CONFIRMED,
        holdUntil = null,
        guestCount = 2,
        phone = "",
        note = "",
    )
}
