package com.thelazybattley.hiddenridge.feature.timeline

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.model.Timeline
import com.thelazybattley.hiddenridge.core.domain.model.allowedShift
import com.thelazybattley.hiddenridge.core.domain.model.canChangeHouse
import com.thelazybattley.hiddenridge.core.domain.model.canMoveOn
import com.thelazybattley.hiddenridge.core.domain.model.conflictsForMove
import com.thelazybattley.hiddenridge.core.domain.model.movedTo
import java.time.YearMonth

/** A booking picked up on the timeline: [dayShift] days from where it was, in [houseId] if it changed house. */
internal data class MoveDraft(
    val bookingId: String,
    val dayShift: Int = 0,
    val houseId: String? = null,
)

/** The booking a draft lifted, as it was and as it would be if put down now. */
internal class MovingBooking(val original: Booking, val moved: Booking)

internal fun Timeline.moving(draft: MoveDraft?): MovingBooking? {
    draft ?: return null
    val original = bookings.firstOrNull { it.id == draft.bookingId }?.takeIf { it.canMoveOn(today) } ?: return null
    val houseIds = draft.houseId?.takeIf { original.canChangeHouse }?.let(::listOf) ?: original.houseIds
    return MovingBooking(original, original.movedTo(original.checkIn.plusDays(draft.dayShift.toLong()), houseIds))
}

/** Every house in the order the timeline lists them, which is the order the up and down arrows step through. */
internal fun Timeline.houseOrder(): List<House> = HouseType.entries.flatMap { type -> houses.filter { it.type == type } }

internal fun Timeline.toUi(draft: MoveDraft? = null): TimelineUi {
    val rows = houseRows(houses)
    val moving = moving(draft)
    val movingState = moving?.let {
        if (bookings.conflictsForMove(it.moved).isEmpty()) MoveState.LIFTED else MoveState.LIFTED_CONFLICT
    } ?: MoveState.NONE
    // The lifted booking is drawn where it would land; the rest stay put and can't be picked up meanwhile.
    val shown = bookings.map { if (it.id == moving?.original?.id) moving.moved else it }
    val bookingsByMode = shown.partition(Booking::isWholeResort)
    val stateOf = { booking: Booking -> if (booking.id == moving?.original?.id) movingState else MoveState.NONE }
    val canMove = { booking: Booking -> moving == null && booking.canMoveOn(today) }
    return TimelineUi(
        today = today,
        todayIndex = window.indexOf(today),
        windowStart = window.start,
        dayCount = window.dayCount,
        houseCount = houses.size,
        months = window.months(),
        rows = rows,
        bookingBars = bookingsByMode.second.flatMap { booking ->
            booking.toBars(window, rows, canMove(booking), stateOf(booking))
        },
        resortBlocks = bookingsByMode.first.map { booking ->
            booking.toResortBlock(window, canMove(booking), stateOf(booking))
        },
        ghosts = moving?.takeIf { it.moved != it.original }?.original?.let { original ->
            if (original.isWholeResort) {
                listOf(GhostBar(rowIndex = null, startIndex = window.indexOf(original.checkIn), nights = original.nights))
            } else {
                original.toBars(window, rows, canMove = false, moveState = MoveState.NONE)
                    .map { GhostBar(rowIndex = it.rowIndex, startIndex = it.startIndex, nights = it.nights) }
            }
        }.orEmpty(),
    )
}

/** What the move bar says about the lifted booking, or null when nothing is lifted. */
internal fun Timeline.moveUi(draft: MoveDraft?, isSaving: Boolean): MoveUi? {
    draft ?: return null
    val moving = moving(draft) ?: return null
    val (original, moved) = moving.original to moving.moved
    val housesById = houses.associateBy(House::id)
    val order = houseOrder()
    val rows = houseRows(houses)
    val shift = draft.dayShift
    val allowed = original.allowedShift(today, window.endExclusive)
    val houseIndex = moved.houseIds.singleOrNull()?.let { id -> order.indexOfFirst { it.id == id } }
    val conflict = bookings.conflictsForMove(moved).firstOrNull()
    return MoveUi(
        bookingId = original.id,
        guestName = original.guestName,
        checkIn = moved.checkIn,
        checkOut = moved.checkOut,
        isWholeResort = original.isWholeResort,
        houseNames = moved.houseIds.mapNotNull { housesById[it]?.name },
        status = when {
            conflict != null -> MoveStatus.Overlaps(
                guestName = conflict.booking.guestName,
                isWholeResort = conflict.booking.isWholeResort,
                houseNames = conflict.houseIds.mapNotNull { housesById[it]?.name },
                checkIn = conflict.booking.checkIn,
                checkOut = conflict.booking.checkOut,
            )
            moved == original -> MoveStatus.NotMoved
            else -> MoveStatus.Ready
        },
        dayShift = shift,
        houseRowIndex = moved.houseIds.singleOrNull()
            ?.let { id -> rows.indexOfFirst { it is TimelineRow.House && it.houseId == id } }
            ?.takeIf { original.canChangeHouse },
        canGoEarlier = shift > allowed.first,
        canGoLater = shift < allowed.last,
        canChangeHouse = original.canChangeHouse,
        canGoUp = original.canChangeHouse && houseIndex != null && houseIndex > 0,
        canGoDown = original.canChangeHouse && houseIndex != null && houseIndex < order.lastIndex,
        isSaving = isSaving,
    )
}

/** Houses grouped under a header per type, types in their natural order. */
private fun houseRows(houses: List<House>): List<TimelineRow> =
    HouseType.entries.flatMap { type ->
        val ofType = houses.filter { it.type == type }
        if (ofType.isEmpty()) {
            emptyList()
        } else {
            listOf(TimelineRow.Group(type)) + ofType.map { TimelineRow.House(houseId = it.id, name = it.name) }
        }
    }

private fun DateWindow.months(): List<TimelineMonth> =
    generateSequence(YearMonth.from(start)) { it.plusMonths(1) }
        .takeWhile { it.atDay(1).isBefore(endExclusive) }
        .map { month ->
            val first = maxOf(month.atDay(1), start)
            val last = minOf(month.atEndOfMonth().plusDays(1), endExclusive)
            TimelineMonth(yearMonth = month, startIndex = indexOf(first), dayCount = indexOf(last) - indexOf(first))
        }
        .toList()

private fun Booking.toBars(
    window: DateWindow,
    rows: List<TimelineRow>,
    canMove: Boolean,
    moveState: MoveState,
): List<BookingBar> =
    houseIds.mapNotNull { houseId ->
        val rowIndex = rows.indexOfFirst { it is TimelineRow.House && it.houseId == houseId }
        val row = rows.getOrNull(rowIndex) as? TimelineRow.House ?: return@mapNotNull null
        BookingBar(
            bookingId = id,
            guestName = guestName,
            houseName = row.name,
            rowIndex = rowIndex,
            startIndex = window.indexOf(checkIn),
            nights = nights,
            checkIn = checkIn,
            checkOut = checkOut,
            isOnHold = isOnHold,
            isGroup = isGroup,
            canMove = canMove,
            moveState = moveState,
        )
    }

private fun Booking.toResortBlock(window: DateWindow, canMove: Boolean, moveState: MoveState) = ResortBlock(
    bookingId = id,
    guestName = guestName,
    startIndex = window.indexOf(checkIn),
    nights = nights,
    checkIn = checkIn,
    checkOut = checkOut,
    isOnHold = isOnHold,
    holdUntil = holdUntil,
    canMove = canMove,
    moveState = moveState,
)
