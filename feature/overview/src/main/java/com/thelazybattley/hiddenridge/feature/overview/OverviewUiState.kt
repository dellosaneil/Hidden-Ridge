package com.thelazybattley.hiddenridge.feature.overview

import androidx.compose.runtime.Immutable
import com.thelazybattley.hiddenridge.core.domain.model.Booking
import java.time.LocalDate
import java.time.YearMonth

internal sealed interface OverviewUiState {
    data object Loading : OverviewUiState

    data class Ready(val overview: OverviewUi) : OverviewUiState
}

@Immutable
internal data class OverviewUi(
    val today: LocalDate,
    val houseCount: Int,
    val occupiedTonight: Int,
    /** Everyone due in today, checked in or not. */
    val arrivals: List<MovementUi>,
    /** Everyone due out today, checked out or not. */
    val departures: List<MovementUi>,
    val holds: HoldSummary,
    /** Arrivals and departures still to be marked, arrivals first. */
    val pendingMoves: List<MovementUi>,
    /** Tonight and the six nights after it. */
    val nights: List<NightUi>,
    /** Holds waiting on staff, most pressing first. */
    val decisions: List<DecisionUi>,
    /** This month and the next two. */
    val months: List<MonthUi>,
    /** The next whole-resort stay in [months], if any. */
    val wholeResort: WholeResortNote?,
) {
    val freeTonight: Int get() = houseCount - occupiedTonight
}

@Immutable
internal data class MovementUi(
    val bookingId: String,
    val guestName: String,
    val houseNames: List<String>,
    val isWholeResort: Boolean,
    val nights: Int,
    val isArrival: Boolean,
    val isDone: Boolean,
)

@Immutable
internal data class HoldSummary(
    val count: Int,
    /** Holds released within a few days. */
    val releasingSoon: Int,
    val noLimit: Int,
    /** Holds with a release date further off. */
    val later: Int,
)

@Immutable
internal data class NightUi(
    val date: LocalDate,
    val booked: Int,
    val held: Int,
)

@Immutable
internal data class DecisionUi(
    /** The hold itself, kept so confirming or releasing it can be undone exactly. */
    val booking: Booking,
    val houses: HousesText,
    val headline: DecisionHeadline,
)

@Immutable
internal sealed interface HousesText {
    data class Names(val names: List<String>) : HousesText

    data class WholeResort(val houseCount: Int) : HousesText
}

/** What makes a hold pressing: its release date, or the guest's arrival. */
@Immutable
internal sealed interface DecisionHeadline {
    data class Releases(val date: LocalDate) : DecisionHeadline

    data class ArrivesIn(val days: Int) : DecisionHeadline

    /** Already arrived without being confirmed. */
    data object InHouse : DecisionHeadline
}

@Immutable
internal data class MonthUi(
    val month: YearMonth,
    /** Share of house-nights booked or held, 0 to 1. */
    val bookedFraction: Float,
)

@Immutable
internal data class WholeResortNote(
    val guestName: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val isHold: Boolean,
)

/** A step staff took from the Overview, confirmed with a way to undo it. */
internal sealed interface OverviewAction {
    data class CheckIn(val move: MovementUi) : OverviewAction

    data class CheckOut(val move: MovementUi) : OverviewAction

    data class Confirm(val decision: DecisionUi) : OverviewAction

    data class Release(val decision: DecisionUi) : OverviewAction
}
