package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import javax.inject.Inject

/** Settles a hold either way, with a way back for an undo. */
class DecideHoldUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend fun confirm(hold: Booking) {
        bookingRepository.updateStatus(bookingId = hold.id, status = BookingStatus.CONFIRMED, holdUntil = null)
    }

    /** Gives the houses back; the booking stays on record as cancelled. */
    suspend fun release(hold: Booking) {
        bookingRepository.updateStatus(bookingId = hold.id, status = BookingStatus.CANCELLED, holdUntil = null)
    }

    /** Puts [hold] back exactly as it was, release date included. */
    suspend fun reopen(hold: Booking) {
        bookingRepository.updateStatus(bookingId = hold.id, status = BookingStatus.HOLD, holdUntil = hold.holdUntil)
    }
}
