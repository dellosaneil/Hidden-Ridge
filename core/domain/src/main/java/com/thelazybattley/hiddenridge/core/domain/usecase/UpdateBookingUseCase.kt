package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingConflict
import com.thelazybattley.hiddenridge.core.domain.model.BookingDraft
import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import com.thelazybattley.hiddenridge.core.domain.model.DraftProblem
import com.thelazybattley.hiddenridge.core.domain.model.conflictsWith
import com.thelazybattley.hiddenridge.core.domain.repository.BookingRepository
import com.thelazybattley.hiddenridge.core.domain.repository.HouseRepository
import com.thelazybattley.hiddenridge.core.domain.time.DateProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Saves an edited draft over an existing booking, for Edit details, unless it's
 * incomplete or another booking already holds its nights. The booking never clashes with itself.
 */
class UpdateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val houseRepository: HouseRepository,
    private val dateProvider: DateProvider,
) {
    sealed interface Result {
        data class Updated(val booking: Booking) : Result

        data class Invalid(val problems: List<DraftProblem>) : Result

        data class Conflicting(val conflicts: List<BookingConflict>) : Result
    }

    suspend operator fun invoke(original: Booking, draft: BookingDraft): Result {
        val today = dateProvider.today()
        val problems = draft.problems(today, keptCheckIn = original.checkIn)
        if (problems.isNotEmpty()) return Result.Invalid(problems)

        val request = checkNotNull(draft.stayRequest()) { "A draft without problems has dates" }
        val houses = houseRepository.observeHouses().first()
        if (!request.isWholeResort && houses.any { it.id in request.houseIds && !it.isOpenOn(request.checkIn) }) {
            return Result.Invalid(listOf(DraftProblem.HOUSE_NOT_OPEN))
        }
        // Check again against the stored bookings: the form's copy may be a moment out of date.
        val conflicts = bookingRepository
            .observeActiveBookings(DateWindow(request.checkIn, request.checkOut))
            .first()
            .filter { it.id != original.id }
            .conflictsWith(request)
        if (conflicts.isNotEmpty()) return Result.Conflicting(conflicts)

        val booking = original.copy(
            guestName = draft.guestName.trim(),
            houseIds = request.houseIds,
            checkIn = request.checkIn,
            checkOut = request.checkOut,
            mode = draft.mode,
            status = draft.status,
            holdUntil = draft.holdUntil(today),
            guestCount = draft.guestCount,
            phone = draft.phone.trim(),
            note = draft.note.trim(),
        )
        bookingRepository.updateBooking(booking)
        return Result.Updated(booking)
    }
}
