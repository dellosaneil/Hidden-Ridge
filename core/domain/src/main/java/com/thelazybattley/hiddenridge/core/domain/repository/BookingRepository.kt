package com.thelazybattley.hiddenridge.core.domain.repository

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    /** Confirmed and held bookings with at least one night inside [window], ordered by check-in. */
    fun observeActiveBookings(window: DateWindow): Flow<List<Booking>>
}
