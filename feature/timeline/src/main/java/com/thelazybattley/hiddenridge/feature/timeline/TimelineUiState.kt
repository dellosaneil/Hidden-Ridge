package com.thelazybattley.hiddenridge.feature.timeline

import androidx.compose.runtime.Immutable
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

internal sealed interface TimelineUiState {
    data object Loading : TimelineUiState

    /** [move] is set while a booking is picked up and being dragged to new dates or another house. */
    data class Ready(val timeline: TimelineUi, val move: MoveUi? = null) : TimelineUiState
}

internal sealed interface TimelineAction {
    /** Lifts a booking off the grid so it can be dragged; from a long press or the details sheet. */
    data class StartMove(val bookingId: String) : TimelineAction

    /** Places the lifted booking [dayShift] days from where it started, in [houseId] if it can change house. */
    data class DragTo(val dayShift: Int, val houseId: String?) : TimelineAction

    /** Steps the lifted booking by [days] and, for a single house, [houses] rows down (negative: up). */
    data class Nudge(val days: Int, val houses: Int) : TimelineAction
    data object CancelMove : TimelineAction
    data object SaveMove : TimelineAction
}

/** Where a booking was put down, for the confirmation after saving. */
internal data class MovedBooking(
    val guestName: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    /** Empty for a whole-resort booking. */
    val houseNames: List<String>,
)

/** The bar shown in place of the navigation bar while a booking is being moved. */
@Immutable
internal data class MoveUi(
    val bookingId: String,
    val guestName: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val isWholeResort: Boolean,
    val houseNames: List<String>,
    val status: MoveStatus,
    /** Days from where the booking started, and the row it sits on now; the drag starts from these. */
    val dayShift: Int,
    val houseRowIndex: Int?,
    val canGoEarlier: Boolean,
    val canGoLater: Boolean,
    val canChangeHouse: Boolean,
    val canGoUp: Boolean,
    val canGoDown: Boolean,
    val isSaving: Boolean,
) {
    val canSave: Boolean get() = status == MoveStatus.Ready && !isSaving
}

@Immutable
internal sealed interface MoveStatus {
    data object NotMoved : MoveStatus

    data object Ready : MoveStatus

    /** The first booking the new spot clashes with. */
    data class Overlaps(
        val guestName: String,
        val isWholeResort: Boolean,
        val houseNames: List<String>,
        val checkIn: LocalDate,
        val checkOut: LocalDate,
    ) : MoveStatus
}

/** How a booking is drawn while one is being moved. */
internal enum class MoveState {
    NONE,

    /** Picked up: raised with a ring, and draggable. */
    LIFTED,

    /** Picked up over nights another booking already has. */
    LIFTED_CONFLICT,
}

/** The dashed outline left where a lifted booking started. Null [rowIndex] spans every row. */
@Immutable
internal data class GhostBar(val rowIndex: Int?, val startIndex: Int, val nights: Int)

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
    val ghosts: List<GhostBar> = emptyList(),
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
    /** Whether a long press can pick it up. */
    val canMove: Boolean = true,
    val moveState: MoveState = MoveState.NONE,
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
    val canMove: Boolean = true,
    val moveState: MoveState = MoveState.NONE,
)
