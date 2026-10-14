package com.thelazybattley.hiddenridge.feature.houses

import androidx.compose.runtime.Immutable
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import java.time.LocalDate

internal sealed interface HousesUiState {
    data object Loading : HousesUiState

    data class Ready(val houses: HousesUi) : HousesUiState
}

/** Which houses the list shows. */
internal enum class HousesFilter {
    ALL,

    /** Free tonight, including houses whose guest leaves today. */
    FREE,

    /** Someone sleeps there tonight. */
    OCCUPIED,

    /** Someone arrives or leaves today. */
    MOVING,
}

@Immutable
internal data class HousesUi(
    val today: LocalDate,
    val houseCount: Int,
    val occupiedTonight: Int,
    val arrivingToday: Int,
    val leavingToday: Int,
    /** Held stays with a night in [week]. */
    val holds: Int,
    /** The nights each row's strip shows, a calendar week holding today. */
    val week: List<LocalDate>,
    val filter: HousesFilter,
    val filterCounts: Map<HousesFilter, Int>,
    /** Group headers and houses that match [filter]. */
    val rows: List<HousesRow>,
) {
    val freeTonight: Int get() = houseCount - occupiedTonight

    val todayIndex: Int get() = week.indexOf(today)
}

@Immutable
internal sealed interface HousesRow {
    data class Group(val type: HouseType) : HousesRow

    /** [isLastInGroup] rows drop the divider below them. */
    data class House(val house: HouseRowUi, val isLastInGroup: Boolean) : HousesRow
}

/** Where a house stands tonight, as its icon tile and badge show it. */
internal enum class HouseStatus {
    OCCUPIED,
    ARRIVING,
    LEAVING,
    FREE,
}

internal enum class NightState {
    FREE,
    BOOKED,
    HOLD,
}

@Immutable
internal data class HouseRowUi(
    val houseId: String,
    val name: String,
    val status: HouseStatus,
    val summary: HouseSummary,
    /** One entry per night of [HousesUi.week]. */
    val nights: List<NightState>,
    /** The stay a tap opens: tonight's, else this morning's departure. Null when the house is free. */
    val bookingId: String?,
)

/** The line under a house's name. */
@Immutable
internal sealed interface HouseSummary {
    data class InHouse(val guestName: String, val night: Int, val nights: Int, val checkOut: LocalDate) : HouseSummary

    data class Arriving(val guestName: String, val nights: Int) : HouseSummary

    data class Leaving(val guestName: String) : HouseSummary

    /** Free tonight; [guestName] arrives on [until], within the week shown. */
    data class FreeUntil(val until: LocalDate, val guestName: String, val isHold: Boolean) : HouseSummary

    data object FreeAllWeek : HouseSummary
}
