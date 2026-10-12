package com.thelazybattley.hiddenridge.feature.availability

import androidx.compose.runtime.Immutable
import com.thelazybattley.hiddenridge.core.domain.model.HouseFilter
import java.time.LocalDate
import java.time.YearMonth

internal sealed interface AvailabilityUiState {
    data object Loading : AvailabilityUiState

    data class Ready(val availability: AvailabilityUi) : AvailabilityUiState
}

internal sealed interface AvailabilityAction {
    data class SetFilter(val filter: HouseFilter) : AvailabilityAction
    data class PickDate(val date: LocalDate) : AvailabilityAction
    data class SetNights(val nights: Int) : AvailabilityAction
    data class ShowMonth(val month: YearMonth) : AvailabilityAction
    data object ShowNextOpening : AvailabilityAction
    data object Clear : AvailabilityAction

    /** Leaves for the booking form with the chosen dates and these houses. */
    data class Book(val houseIds: List<String>, val wholeResort: Boolean = false) : AvailabilityAction

    /** Opens a booking that's in the way. */
    data class ViewBooking(val bookingId: String) : AvailabilityAction
}

@Immutable
internal data class AvailabilityUi(
    val filter: HouseFilter,
    val filters: List<HouseFilter>,
    val checkIn: LocalDate?,
    val checkOut: LocalDate?,
    val nights: Int,
    val calendar: AvailabilityCalendarUi,
    /** Set once both dates are picked. */
    val result: AvailabilityResultUi?,
) {
    val isWholeResort: Boolean get() = filter == HouseFilter.WholeResort
}

@Immutable
internal data class AvailabilityCalendarUi(
    val month: YearMonth,
    val days: List<AvailabilityDayUi>,
    val canGoBack: Boolean,
    val canGoForward: Boolean,
)

@Immutable
internal data class AvailabilityDayUi(
    val date: LocalDate,
    val isPast: Boolean,
    val isToday: Boolean,
    val tone: DayTone,
    /** Free houses that night among those the filter covers; for the whole resort, 1 when all are free. */
    val freeCount: Int,
    val total: Int,
    /** The whole-resort booking on this night, if any. */
    val resortGuest: String?,
    val selection: DaySelection,
)

/** [PAST] nights can't be booked, and bookings before today aren't loaded, so they show no count. */
internal enum class DayTone { PAST, ALL_FREE, SOME_FREE, FULL, RESORT_BOOKED, RESORT_HOLD }

internal enum class DaySelection { NONE, ENDPOINT, INSIDE }

@Immutable
internal sealed interface AvailabilityResultUi {
    val checkIn: LocalDate
    val checkOut: LocalDate
    val nights: Int

    /** When nothing fits, the first check-in with the same length of stay that does. */
    val nextOpening: LocalDate?

    data class Houses(
        override val checkIn: LocalDate,
        override val checkOut: LocalDate,
        override val nights: Int,
        override val nextOpening: LocalDate?,
        val filter: HouseFilter,
        val rows: List<HouseRowUi>,
    ) : AvailabilityResultUi {
        val freeRows: List<HouseRowUi> get() = rows.filter { it.takenBy == null }
        val fits: Boolean get() = freeRows.isNotEmpty()
    }

    data class WholeResort(
        override val checkIn: LocalDate,
        override val checkOut: LocalDate,
        override val nights: Int,
        override val nextOpening: LocalDate?,
        val conflicts: List<ConflictRowUi>,
        val houseCount: Int,
    ) : AvailabilityResultUi {
        val fits: Boolean get() = conflicts.isEmpty()
    }
}

@Immutable
internal data class HouseRowUi(
    val houseId: String,
    val name: String,
    val sleeps: Int,
    val takenBy: ConflictRowUi?,
)

/** An existing booking that holds some of the chosen nights. */
@Immutable
internal data class ConflictRowUi(
    val bookingId: String,
    val guestName: String,
    val isHold: Boolean,
    val isWholeResort: Boolean,
    val houseNames: List<String>,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
)
