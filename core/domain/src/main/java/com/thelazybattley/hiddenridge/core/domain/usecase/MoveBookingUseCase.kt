package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingConflict
import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import com.thelazybattley.hiddenridge.core.domain.model.MoveProblem
import com.thelazybattley.hiddenridge.core.domain.model.conflictsForMove
import com.thelazybattley.hiddenridge.core.domain.model.moveProblem
import com.thelazybattley.hiddenridge.core.domain.model.movedTo
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import com.thelazybattley.hiddenridge.core.domain.time.DateProvider
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Shifts a booking to new dates and, for a single house, another house, unless that clashes. */
class MoveBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val dateProvider: DateProvider,
) {
    sealed interface Result {
        data class Moved(val booking: Booking) : Result

        data class Invalid(val problem: MoveProblem) : Result

        data class Conflicting(val conflicts: List<BookingConflict>) : Result
    }

    suspend operator fun invoke(booking: Booking, checkIn: LocalDate, houseIds: List<String>): Result {
        val today = dateProvider.today()
        val until = DateWindow.timelineAround(today).endExclusive
        booking.moveProblem(checkIn, houseIds, today, until)?.let { return Result.Invalid(it) }

        val moved = booking.movedTo(checkIn, houseIds)
        // Check again against the stored bookings: the timeline's copy may be a moment out of date.
        val conflicts = bookingRepository
            .observeActiveBookings(DateWindow(moved.checkIn, moved.checkOut))
            .first()
            .conflictsForMove(moved)
        if (conflicts.isNotEmpty()) return Result.Conflicting(conflicts)

        bookingRepository.updateBooking(moved)
        return Result.Moved(moved)
    }
}
