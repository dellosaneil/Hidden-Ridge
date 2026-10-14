package com.thelazybattley.hiddenridge.feature.timeline

import androidx.compose.runtime.Immutable
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

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
    /** Share of house-nights booked, 0 to 1; a whole-resort stay books every house. */
    val bookedFraction: Float = 0f,
    /** A whole-resort booking has at least one night this month. */
    val hasWholeResort: Boolean = false,
) {
    val bookedPercent: Int get() = (bookedFraction * 100).roundToInt()
}

@Immutable
internal sealed interface TimelineRow {
    data class Group(val type: HouseType) : TimelineRow

    /** [openIndex] is the first day guests can stay, when the house opens inside the window; days before it are closed. */
    data class House(val houseId: String, val name: String, val openIndex: Int? = null) : TimelineRow
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
