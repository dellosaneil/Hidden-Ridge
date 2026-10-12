package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import com.thelazybattley.hiddenridge.core.domain.time.DateProvider
import java.time.LocalDate
import javax.inject.Inject

class PutBookingOnHoldUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val dateProvider: DateProvider,
) {
    suspend operator fun invoke(booking: Booking) {
        bookingRepository.updateStatus(
            bookingId = booking.id,
            status = BookingStatus.HOLD,
            holdUntil = holdUntil(today = dateProvider.today(), checkIn = booking.checkIn),
        )
    }

    companion object {
        private const val DEFAULT_HOLD_DAYS = 3L

        /** A few days to confirm, but never past check-in and never before today. */
        fun holdUntil(today: LocalDate, checkIn: LocalDate): LocalDate =
            minOf(today.plusDays(DEFAULT_HOLD_DAYS), checkIn).coerceAtLeast(today)
    }
}
