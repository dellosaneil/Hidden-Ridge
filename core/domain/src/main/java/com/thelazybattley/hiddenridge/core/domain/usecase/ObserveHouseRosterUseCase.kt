package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import com.thelazybattley.hiddenridge.core.domain.model.HouseRoster
import com.thelazybattley.hiddenridge.core.domain.model.houseRoster
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import com.thelazybattley.hiddenridge.core.domain.repository.HouseRepository
import com.thelazybattley.hiddenridge.core.domain.time.DateProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveHouseRosterUseCase @Inject constructor(
    private val houseRepository: HouseRepository,
    private val bookingRepository: BookingRepository,
    private val dateProvider: DateProvider,
) {
    operator fun invoke(): Flow<HouseRoster> {
        val today = dateProvider.today()
        // From a week back, so this week's earlier nights and this morning's check-outs are included,
        // to as far ahead as anyone can book.
        val window = DateWindow(start = today.minusDays(DAYS_BACK), endExclusive = DateWindow.timelineAround(today).endExclusive)
        return combine(
            houseRepository.observeHouses(),
            bookingRepository.observeActiveBookings(window),
        ) { houses, bookings ->
            houseRoster(today = today, houses = houses, bookings = bookings)
        }
    }

    private companion object {
        const val DAYS_BACK = 7L
    }
}
