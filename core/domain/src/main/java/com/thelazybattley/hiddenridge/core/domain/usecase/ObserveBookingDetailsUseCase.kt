package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.BookingDetails
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import com.thelazybattley.hiddenridge.core.domain.repository.HouseRepository
import com.thelazybattley.hiddenridge.core.domain.time.DateProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveBookingDetailsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val houseRepository: HouseRepository,
    private val dateProvider: DateProvider,
) {
    /** Emits null once the booking has been deleted. */
    operator fun invoke(bookingId: String): Flow<BookingDetails?> =
        combine(
            bookingRepository.observeBooking(bookingId),
            houseRepository.observeHouses(),
        ) { booking, houses ->
            booking?.let {
                BookingDetails(
                    booking = it,
                    houses = if (it.isWholeResort) houses else houses.filter { house -> house.id in it.houseIds },
                    today = dateProvider.today(),
                )
            }
        }
}
