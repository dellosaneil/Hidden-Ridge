package com.thelazybattley.hiddenridge.feature.bookings

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.YearMonth

internal sealed interface BookingsUiState {
    data object Loading : BookingsUiState

    data class Ready(val bookings: BookingsUi) : BookingsUiState
}

internal enum class BookingsTab {
    /** Who is here today, then everything still to come. */
    UPCOMING,

    /** Stays that have ended, and every cancelled booking. */
    PAST,
}

/** What kind of booking a row is, for the status chips. Each booking is exactly one. */
internal enum class BookingsFilter {
    ALL,
    CONFIRMED,
    HOLD,
    WHOLE_RESORT,
}

@Immutable
internal data class BookingsUi(
    val today: LocalDate,
    val tab: BookingsTab,
    val filter: BookingsFilter,
    val tabCounts: Map<BookingsTab, Int>,
    /** Counts within [tab]. */
    val filterCounts: Map<BookingsFilter, Int>,
    val rows: List<BookingsRow>,
)

@Immutable
internal sealed interface BookingsRow {
    data class Group(val label: GroupLabel, val count: Int) : BookingsRow

    /** [isLastInGroup] rows drop the divider below them. */
    data class Booking(val booking: BookingRowUi, val isLastInGroup: Boolean) : BookingsRow
}

@Immutable
internal sealed interface GroupLabel {
    /** Arriving, leaving and staying guests. */
    data class Today(val date: LocalDate) : GroupLabel

    /** Arrivals later in the calendar week. */
    data object ThisWeek : GroupLabel

    /** Past stays from earlier in the current month. */
    data class EarlierIn(val month: YearMonth) : GroupLabel

    data class Month(val month: YearMonth, val showYear: Boolean) : GroupLabel
}

/** Where a booking stands, as its pill says. */
internal enum class RowStatus {
    ARRIVING,
    LEAVING,
    IN_HOUSE,
    CHECKED_OUT,
    CANCELLED,
    ON_HOLD,
    WHOLE_RESORT,
    CONFIRMED,
}

@Immutable
internal data class BookingRowUi(
    val bookingId: String,
    val guestName: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val nights: Int,
    val houses: HousesLabel,
    val status: RowStatus,
    val kind: BookingsFilter,
    /** In today's group, so its date tile is highlighted. */
    val isToday: Boolean,
    /** Dates as weekdays, "Wed 14 – Sat 17", for arrivals this week. */
    val showWeekdays: Boolean,
    /** Shown for holds that lapse on a set day. */
    val holdUntil: LocalDate?,
) {
    val isWholeResort: Boolean get() = kind == BookingsFilter.WHOLE_RESORT
    val isHold: Boolean get() = kind == BookingsFilter.HOLD
}

@Immutable
internal sealed interface HousesLabel {
    data class Names(val names: List<String>) : HousesLabel

    data class WholeResort(val houseCount: Int) : HousesLabel
}
