package com.thelazybattley.hiddenridge.feature.availability

import com.thelazybattley.hiddenridge.core.domain.model.Availability
import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseFilter
import com.thelazybattley.hiddenridge.core.domain.model.StayRequest
import com.thelazybattley.hiddenridge.core.domain.model.housesFor
import com.thelazybattley.hiddenridge.core.domain.model.nextOpening
import com.thelazybattley.hiddenridge.core.domain.model.occupancyOn
import com.thelazybattley.hiddenridge.core.domain.model.types
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/** What the user has picked; the rest of the screen is derived from this plus [Availability]. */
internal data class AvailabilityForm(
    val filter: HouseFilter = HouseFilter.AnyHouse,
    val checkIn: LocalDate? = null,
    val checkOut: LocalDate? = null,
    /** The month the calendar shows; null follows check-in, or today before one is picked. */
    val visibleMonth: YearMonth? = null,
) {
    val hasStay: Boolean get() = checkIn != null && checkOut != null && checkOut.isAfter(checkIn)

    val nights: Int get() = if (hasStay) ChronoUnit.DAYS.between(checkIn, checkOut).toInt() else 0

    /** A first tap picks check-in, a later one check-out; tapping again starts over. */
    fun picking(date: LocalDate): AvailabilityForm =
        if (checkIn == null || checkOut != null || !date.isAfter(checkIn)) {
            copy(checkIn = date, checkOut = null)
        } else {
            copy(checkOut = date)
        }

    fun withNights(nights: Int): AvailabilityForm =
        checkIn?.let { copy(checkOut = it.plusDays(nights.toLong())) } ?: this

    fun startingOn(checkIn: LocalDate): AvailabilityForm =
        copy(checkIn = checkIn, checkOut = checkIn.plusDays(nights.toLong()), visibleMonth = YearMonth.from(checkIn))
}

internal fun AvailabilityForm.toUi(availability: Availability): AvailabilityUi = AvailabilityUi(
    filter = filter,
    filters = listOf(HouseFilter.AnyHouse) +
        availability.houses.types().map(HouseFilter::OfType) +
        HouseFilter.WholeResort,
    checkIn = checkIn,
    checkOut = checkOut,
    nights = nights,
    calendar = calendar(availability),
    result = if (hasStay) result(availability, checkIn!!, checkOut!!) else null,
)

private fun AvailabilityForm.calendar(availability: Availability): AvailabilityCalendarUi {
    val firstMonth = YearMonth.from(availability.today)
    val lastMonth = YearMonth.from(availability.window.endExclusive.minusDays(1))
    val month = (visibleMonth ?: checkIn?.let(YearMonth::from) ?: firstMonth).coerceIn(firstMonth, lastMonth)
    val houses = availability.housesFor(filter)
    val wholeResort = filter == HouseFilter.WholeResort
    val total = if (wholeResort) 1 else houses.size
    val stayEnd = checkOut?.takeIf { hasStay }

    return AvailabilityCalendarUi(
        month = month,
        days = (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            val occupancy = availability.occupancyOn(date)
            val free = if (wholeResort) {
                if (occupancy.takenHouseIds.isEmpty()) 1 else 0
            } else {
                houses.count { it.id !in occupancy.takenHouseIds && it.id !in occupancy.closedHouseIds }
            }
            val resortBooking = occupancy.wholeResortBooking
            val isPast = date.isBefore(availability.today)
            AvailabilityDayUi(
                date = date,
                isPast = isPast,
                isToday = date == availability.today,
                tone = when {
                    isPast -> DayTone.PAST
                    resortBooking != null -> if (resortBooking.isOnHold) DayTone.RESORT_HOLD else DayTone.RESORT_BOOKED
                    free == 0 -> DayTone.FULL
                    free == total -> DayTone.ALL_FREE
                    else -> DayTone.SOME_FREE
                },
                freeCount = free,
                total = total,
                resortGuest = resortBooking?.guestName?.takeUnless { isPast },
                selection = when {
                    date == checkIn || date == stayEnd -> DaySelection.ENDPOINT
                    stayEnd != null && date.isAfter(checkIn) && date.isBefore(stayEnd) -> DaySelection.INSIDE
                    else -> DaySelection.NONE
                },
            )
        },
        canGoBack = month > firstMonth,
        canGoForward = month < lastMonth,
    )
}

private fun AvailabilityForm.result(availability: Availability, checkIn: LocalDate, checkOut: LocalDate): AvailabilityResultUi {
    val housesById = availability.houses.associateBy(House::id)
    val nextOpening by lazy { availability.nextOpening(filter, from = checkIn.plusDays(1), nights = nights) }

    if (filter == HouseFilter.WholeResort) {
        val conflicts = availability.conflictsFor(StayRequest(BookingMode.WHOLE_RESORT, emptyList(), checkIn, checkOut))
            .map { it.booking.toRow(housesById) }
        return AvailabilityResultUi.WholeResort(
            checkIn = checkIn,
            checkOut = checkOut,
            nights = nights,
            nextOpening = if (conflicts.isEmpty()) null else nextOpening,
            conflicts = conflicts,
            houseCount = availability.houses.size,
        )
    }

    val rows = availability.housesFor(filter).map { house ->
        val conflict = availability.conflictsFor(StayRequest(BookingMode.INDIVIDUAL, listOf(house.id), checkIn, checkOut)).firstOrNull()
        HouseRowUi(
            houseId = house.id,
            name = house.name,
            sleeps = house.sleeps,
            takenBy = conflict?.booking?.toRow(housesById),
            opensOn = house.bookableFrom?.takeUnless { house.isOpenOn(checkIn) },
        )
    }
    return AvailabilityResultUi.Houses(
        checkIn = checkIn,
        checkOut = checkOut,
        nights = nights,
        nextOpening = if (rows.any(HouseRowUi::isFree)) null else nextOpening,
        filter = filter,
        rows = rows,
    )
}

private fun Booking.toRow(housesById: Map<String, House>) = ConflictRowUi(
    bookingId = id,
    guestName = guestName,
    isHold = isOnHold,
    isWholeResort = isWholeResort,
    houseNames = houseIds.mapNotNull { housesById[it]?.name },
    checkIn = checkIn,
    checkOut = checkOut,
)
