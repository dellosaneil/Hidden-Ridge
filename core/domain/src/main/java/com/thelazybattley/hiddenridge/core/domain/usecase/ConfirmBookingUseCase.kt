package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import javax.inject.Inject

class ConfirmBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(booking: Booking) {
        bookingRepository.updateStatus(bookingId = booking.id, status = BookingStatus.CONFIRMED, holdUntil = null)
    }
}
