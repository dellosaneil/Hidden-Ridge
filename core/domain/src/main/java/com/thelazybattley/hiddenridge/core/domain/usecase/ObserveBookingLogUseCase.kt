package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import com.thelazybattley.hiddenridge.core.domain.repository.HouseRepository
import com.thelazybattley.hiddenridge.core.domain.time.DateProvider
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Every booking ever made, cancelled ones too, with the houses they name, as seen on [today]. */
data class BookingLog(
    val today: LocalDate,
    val houses: List<House>,
    val bookings: List<Booking>,
)

class ObserveBookingLogUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val houseRepository: HouseRepository,
    private val dateProvider: DateProvider,
) {
    operator fun invoke(): Flow<BookingLog> =
        combine(bookingRepository.observeAllBookings(), houseRepository.observeHouses()) { bookings, houses ->
            BookingLog(today = dateProvider.today(), houses = houses, bookings = bookings)
        }
}
