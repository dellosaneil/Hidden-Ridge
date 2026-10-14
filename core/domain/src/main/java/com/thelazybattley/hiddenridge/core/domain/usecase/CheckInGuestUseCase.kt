package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import javax.inject.Inject

/** Marks a guest arriving today as checked in, so their houses show as occupied. */
class CheckInGuestUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(bookingId: String) {
        bookingRepository.setCheckedIn(bookingId, checkedIn = true)
    }

    suspend fun undo(bookingId: String) {
        bookingRepository.setCheckedIn(bookingId, checkedIn = false)
    }
}
