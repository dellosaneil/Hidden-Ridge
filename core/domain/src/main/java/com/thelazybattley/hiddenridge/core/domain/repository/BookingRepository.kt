package com.thelazybattley.hiddenridge.core.domain.repository

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    /** Confirmed and held bookings with at least one night inside [window], ordered by check-in. */
    fun observeActiveBookings(window: DateWindow): Flow<List<Booking>>

    /** The booking with [bookingId] in any status, or null once it no longer exists. */
    fun observeBooking(bookingId: String): Flow<Booking?>

    suspend fun createBooking(booking: Booking)

    suspend fun updateStatus(bookingId: String, status: BookingStatus, holdUntil: LocalDate?)

    /** Saves every field of an existing booking, including the houses it occupies. */
    suspend fun updateBooking(booking: Booking)

    suspend fun deleteBooking(bookingId: String)
}
