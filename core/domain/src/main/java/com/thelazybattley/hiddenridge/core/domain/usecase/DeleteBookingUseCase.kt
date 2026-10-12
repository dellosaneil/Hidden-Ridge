package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import javax.inject.Inject

/** Removes the booking and its history entirely. Prefer [CancelBookingUseCase] when a guest cancels. */
class DeleteBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(booking: Booking) {
        bookingRepository.deleteBooking(booking.id)
    }
}
