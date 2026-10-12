package com.thelazybattley.hiddenridge.core.data.repository

import com.thelazybattley.hiddenridge.core.data.mapper.toDomain
import com.thelazybattley.hiddenridge.core.database.dao.BookingDao
import com.thelazybattley.hiddenridge.core.database.entity.BookingWithHouses
import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class OfflineBookingRepository @Inject constructor(
    private val bookingDao: BookingDao,
) : BookingRepository {

    override fun observeActiveBookings(window: DateWindow): Flow<List<Booking>> =
        bookingDao.observeActiveBetween(from = window.start, to = window.endExclusive)
            .map { bookings -> bookings.map(BookingWithHouses::toDomain) }
}
