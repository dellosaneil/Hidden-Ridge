package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.Availability
import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import com.thelazybattley.hiddenridge.core.domain.repository.HouseRepository
import com.thelazybattley.hiddenridge.core.domain.time.DateProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** What can be booked, from today to the end of the bookable window. */
class ObserveAvailabilityUseCase @Inject constructor(
    private val houseRepository: HouseRepository,
    private val bookingRepository: BookingRepository,
    private val dateProvider: DateProvider,
) {
    operator fun invoke(): Flow<Availability> {
        val today = dateProvider.today()
        val window = DateWindow(start = today, endExclusive = DateWindow.timelineAround(today).endExclusive)
        return combine(
            houseRepository.observeHouses(),
            bookingRepository.observeActiveBookings(window),
        ) { houses, bookings ->
            Availability(today = today, window = window, houses = houses, bookings = bookings)
        }
    }
}
