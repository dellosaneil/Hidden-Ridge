package com.thelazybattley.hiddenridge.feature.newbooking

import com.thelazybattley.hiddenridge.core.domain.model.Availability
import com.thelazybattley.hiddenridge.core.domain.model.BookingConflict
import com.thelazybattley.hiddenridge.core.domain.model.BookingDraft
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.model.StayRequest
import java.time.LocalDate
import java.time.YearMonth

/** Everything the user has entered or toggled; the screen is derived from this plus [Availability]. */
internal data class NewBookingForm(
    val draft: BookingDraft,
    val editingDate: DateField = DateField.CHECK_IN,
    /** The month the calendar shows; null follows check-in, or today before one is picked. */
    val visibleMonth: YearMonth? = null,
    /** The house type whose houses are listed; null follows the first selected house. */
    val houseTypeTab: HouseType? = null,
    val isMonthPickerOpen: Boolean = false,
    val showProblems: Boolean = false,
    val isSaving: Boolean = false,
) {
    companion object {
        /** Prefilled from the timeline: a tapped house row and night, one night long. */
        fun startingWith(houseId: String?, checkIn: LocalDate?) = NewBookingForm(
            draft = BookingDraft(
                houseIds = listOfNotNull(houseId),
                checkIn = checkIn,
                checkOut = checkIn?.plusDays(1),
            ),
        )
    }
}

internal fun NewBookingForm.toUi(availability: Availability): NewBookingFormUi {
    val request = draft.stayRequest()
    val housesById = availability.houses.associateBy(House::id)
    val selectedHouses = availability.houses.filter { it.id in draft.houseIds }
    val activeType = houseTypeTab
        ?: selectedHouses.firstOrNull()?.type
        ?: availability.houses.firstOrNull()?.type
        ?: HouseType.CABIN

    return NewBookingFormUi(
        draft = draft,
        editingDate = editingDate,
        calendar = calendar(availability),
        houseTypes = HouseType.entries
            .mapNotNull { type -> availability.houses.filter { it.type == type }.takeIf { it.isNotEmpty() }?.let { type to it } }
            .map { (type, houses) ->
                HouseTypeTileUi(
                    type = type,
                    sleeps = type.sleeps,
                    freeCount = request?.let { houses.count { availability.isFree(it.id, request.checkIn, request.checkOut) } },
                    selectedCount = houses.count { it.id in draft.houseIds },
                    isActive = type == activeType,
                )
            },
        houses = availability.houses.filter { it.type == activeType }.map { houseTile(it, availability, request) },
        selectedHouseNames = selectedHouses.map(House::name),
        houseCountsByType = availability.houses.groupingBy(House::type).eachCount()
            .map { (type, count) -> HouseTypeCount(type, count) }
            .sortedBy { it.type.ordinal },
        totalHouses = availability.houses.size,
        capacity = (if (draft.isWholeResort) availability.houses else selectedHouses).sumOf(House::sleeps),
        holdUntil = draft.holdUntil(availability.today),
        conflicts = request?.let(availability::conflictsFor).orEmpty().map { it.toUi(housesById) },
        problems = draft.problems(availability.today),
        showProblems = showProblems,
        isSaving = isSaving,
    )
}

private fun NewBookingForm.calendar(availability: Availability): CalendarUi {
    val firstMonth = YearMonth.from(availability.today)
    val lastMonth = YearMonth.from(availability.window.endExclusive.minusDays(1))
    val month = (visibleMonth ?: draft.checkIn?.let(YearMonth::from) ?: firstMonth).coerceIn(firstMonth, lastMonth)
    val blocked = availability.blockedNights(
        mode = draft.mode,
        houseIds = draft.houseIds,
        from = month.atDay(1),
        until = month.atEndOfMonth().plusDays(1),
    )
    val checkIn = draft.checkIn
    val checkOut = draft.checkOut?.takeIf { draft.hasDates }
    return CalendarUi(
        month = month,
        days = (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            CalendarDayUi(
                date = date,
                isPast = date.isBefore(availability.today),
                isToday = date == availability.today,
                isBlocked = date in blocked,
                isEndpoint = date == checkIn || date == checkOut,
                band = when {
                    checkIn == null || checkOut == null -> RangeBand.NONE
                    date == checkIn -> RangeBand.START_HALF
                    date == checkOut -> RangeBand.END_HALF
                    date.isAfter(checkIn) && date.isBefore(checkOut) -> RangeBand.FULL
                    else -> RangeBand.NONE
                },
            )
        },
        firstMonth = firstMonth,
        lastMonth = lastMonth,
        isMonthPickerOpen = isMonthPickerOpen,
    )
}

private fun NewBookingForm.houseTile(house: House, availability: Availability, request: StayRequest?): HouseTileUi {
    val selected = house.id in draft.houseIds
    val conflict = request?.let {
        availability.conflictsFor(StayRequest(BookingMode.INDIVIDUAL, listOf(house.id), it.checkIn, it.checkOut)).firstOrNull()
    }
    return HouseTileUi(
        houseId = house.id,
        name = house.name,
        shortLabel = house.shortLabel(),
        state = when {
            request == null -> if (selected) HouseTileState.SELECTED else HouseTileState.NEEDS_DATES
            conflict != null && selected -> HouseTileState.SELECTED_BUT_TAKEN
            conflict != null -> HouseTileState.TAKEN
            selected -> HouseTileState.SELECTED
            else -> HouseTileState.FREE
        },
        takenBy = conflict?.booking?.let {
            TakenBy(guestName = it.guestName, isWholeResort = it.isWholeResort, checkIn = it.checkIn, checkOut = it.checkOut)
        },
    )
}

/** The big label on a house tile: "1" for Cabin 1, "A" for Casita A, "Ridge" for Ridge House. */
internal fun House.shortLabel(): String {
    val words = name.split(' ').filter(String::isNotBlank)
    val last = words.lastOrNull().orEmpty()
    return if (last.length <= 2) last else words.firstOrNull().orEmpty()
}

private fun BookingConflict.toUi(housesById: Map<String, House>) = ConflictUi(
    guestName = booking.guestName,
    isWholeResort = booking.isWholeResort,
    houseNames = houseIds.mapNotNull { housesById[it]?.name },
    checkIn = booking.checkIn,
    checkOut = booking.checkOut,
)
