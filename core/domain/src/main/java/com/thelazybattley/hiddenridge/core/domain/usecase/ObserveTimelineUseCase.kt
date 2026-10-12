package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import com.thelazybattley.hiddenridge.core.domain.model.Timeline
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import com.thelazybattley.hiddenridge.core.domain.repository.HouseRepository
import com.thelazybattley.hiddenridge.core.domain.time.DateProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveTimelineUseCase @Inject constructor(
    private val houseRepository: HouseRepository,
    private val bookingRepository: BookingRepository,
    private val dateProvider: DateProvider,
) {
    operator fun invoke(): Flow<Timeline> {
        val today = dateProvider.today()
        val window = DateWindow.timelineAround(today)
        return combine(
            houseRepository.observeHouses(),
            bookingRepository.observeActiveBookings(window),
        ) { houses, bookings ->
            Timeline(today = today, window = window, houses = houses, bookings = bookings)
        }
    }
}
