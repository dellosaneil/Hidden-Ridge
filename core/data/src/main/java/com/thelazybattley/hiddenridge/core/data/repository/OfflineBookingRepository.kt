package com.thelazybattley.hiddenridge.core.data.repository

import com.thelazybattley.hiddenridge.core.data.mapper.toDomain
import com.thelazybattley.hiddenridge.core.data.mapper.toEntity
import com.thelazybattley.hiddenridge.core.data.mapper.toHouseRefs
import com.thelazybattley.hiddenridge.core.database.dao.BookingDao
import com.thelazybattley.hiddenridge.core.database.entity.BookingWithHouses
import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class OfflineBookingRepository @Inject constructor(
    private val bookingDao: BookingDao,
) : BookingRepository {

    override fun observeActiveBookings(window: DateWindow): Flow<List<Booking>> =
        bookingDao.observeActiveBetween(from = window.start, to = window.endExclusive)
            .map { bookings -> bookings.map(BookingWithHouses::toDomain) }

    override fun observeBooking(bookingId: String): Flow<Booking?> =
        bookingDao.observeById(bookingId).map { it?.toDomain() }

    override suspend fun createBooking(booking: Booking) {
        bookingDao.upsertAll(bookings = listOf(booking.toEntity()), bookingHouses = booking.toHouseRefs())
    }

    override suspend fun updateStatus(bookingId: String, status: BookingStatus, holdUntil: LocalDate?) {
        bookingDao.updateStatus(id = bookingId, status = status.name, holdUntil = holdUntil)
    }

    override suspend fun updateBooking(booking: Booking) {
        bookingDao.updateWithHouses(booking = booking.toEntity(), bookingHouses = booking.toHouseRefs())
    }

    override suspend fun deleteBooking(bookingId: String) {
        bookingDao.deleteById(bookingId)
    }
}
