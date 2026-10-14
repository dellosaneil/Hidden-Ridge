package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import javax.inject.Inject

/** Marks a guest leaving today as checked out, so their houses show as free. */
class CheckOutGuestUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
) {
    suspend operator fun invoke(bookingId: String) {
        bookingRepository.setCheckedOut(bookingId, checkedOut = true)
    }

    suspend fun undo(bookingId: String) {
        bookingRepository.setCheckedOut(bookingId, checkedOut = false)
    }
}
