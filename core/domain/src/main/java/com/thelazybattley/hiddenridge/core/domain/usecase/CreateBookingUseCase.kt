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
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Saves a draft as a booking, unless it's incomplete or another booking already holds its nights. */
class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val houseRepository: HouseRepository,
    private val dateProvider: DateProvider,
) {
    sealed interface Result {
        data class Created(val booking: Booking) : Result

        data class Invalid(val problems: List<DraftProblem>) : Result

        data class Conflicting(val conflicts: List<BookingConflict>) : Result
    }

    suspend operator fun invoke(draft: BookingDraft): Result {
        val today = dateProvider.today()
        val problems = draft.problems(today)
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
            .conflictsWith(request)
        if (conflicts.isNotEmpty()) return Result.Conflicting(conflicts)

        val booking = Booking(
            id = UUID.randomUUID().toString(),
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
        bookingRepository.createBooking(booking)
        return Result.Created(booking)
    }
}
