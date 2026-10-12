package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.HoldRelease
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import com.thelazybattley.hiddenridge.core.domain.time.DateProvider
import javax.inject.Inject

class PutBookingOnHoldUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val dateProvider: DateProvider,
) {
    suspend operator fun invoke(booking: Booking) {
        bookingRepository.updateStatus(
            bookingId = booking.id,
            status = BookingStatus.HOLD,
            holdUntil = HoldRelease.date(today = dateProvider.today(), checkIn = booking.checkIn),
        )
    }
}
