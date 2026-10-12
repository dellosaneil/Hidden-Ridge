package com.thelazybattley.hiddenridge.feature.timeline

import androidx.compose.runtime.Immutable
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import java.time.LocalDate
import java.time.YearMonth

internal sealed interface TimelineUiState {
    data object Loading : TimelineUiState

    data class Ready(val timeline: TimelineUi) : TimelineUiState
}

/** The timeline laid out as day columns (indexed from [windowStart]) and house rows. */
@Immutable
internal data class TimelineUi(
    val today: LocalDate,
    val todayIndex: Int,
    val windowStart: LocalDate,
    val dayCount: Int,
    val houseCount: Int,
    val months: List<TimelineMonth>,
    val rows: List<TimelineRow>,
    val bookingBars: List<BookingBar>,
    val resortBlocks: List<ResortBlock>,
) {
    fun dateAt(dayIndex: Int): LocalDate = windowStart.plusDays(dayIndex.toLong())

    fun monthIndexAt(dayIndex: Int): Int = months.indexOfLast { it.startIndex <= dayIndex }.coerceAtLeast(0)
}

@Immutable
internal data class TimelineMonth(
    val yearMonth: YearMonth,
    val startIndex: Int,
    val dayCount: Int,
)

@Immutable
internal sealed interface TimelineRow {
    data class Group(val type: HouseType) : TimelineRow

    data class House(val houseId: String, val name: String) : TimelineRow
}

/** One house's share of an individual or group booking. */
@Immutable
internal data class BookingBar(
    val bookingId: String,
    val guestName: String,
    val houseName: String,
    val rowIndex: Int,
    val startIndex: Int,
    val nights: Int,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val isOnHold: Boolean,
    val isGroup: Boolean,
) {
    val key: String get() = "$bookingId:$rowIndex"
}

/** A whole-resort booking, drawn across every house row. */
@Immutable
internal data class ResortBlock(
    val bookingId: String,
    val guestName: String,
    val startIndex: Int,
    val nights: Int,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val isOnHold: Boolean,
    val holdUntil: LocalDate?,
)
