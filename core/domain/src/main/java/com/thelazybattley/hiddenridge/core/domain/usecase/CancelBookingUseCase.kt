package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import javax.inject.Inject

/** Frees the booking's houses but keeps it on record, unlike [DeleteBookingUseCase]. */
class CancelBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(booking: Booking) {
        bookingRepository.updateStatus(bookingId = booking.id, status = BookingStatus.CANCELLED, holdUntil = null)
    }
}
