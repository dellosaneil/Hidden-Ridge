package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate

/** What a guest is asking for when staff check availability. */
sealed interface HouseFilter {
    /** Any single house will do. */
    data object AnyHouse : HouseFilter

    /** Any house of one type. */
    data class OfType(val type: HouseType) : HouseFilter

    /** Every house at once, for one group. */
    data object WholeResort : HouseFilter
}

/** The houses [filter] is about. */
fun Availability.housesFor(filter: HouseFilter): List<House> = when (filter) {
    HouseFilter.AnyHouse, HouseFilter.WholeResort -> houses
    is HouseFilter.OfType -> houses.filter { it.type == filter.type }
}

/** Whether the stay can be booked: the whole resort is free, or at least one matching house is. */
fun Availability.fits(filter: HouseFilter, checkIn: LocalDate, checkOut: LocalDate): Boolean = when (filter) {
    HouseFilter.WholeResort -> conflictsFor(StayRequest(BookingMode.WHOLE_RESORT, emptyList(), checkIn, checkOut)).isEmpty()
    else -> housesFor(filter).any { isFree(it.id, checkIn, checkOut) }
}

/** The first check-in on or after [from] where a stay of [nights] fits, within the bookable window. */
fun Availability.nextOpening(filter: HouseFilter, from: LocalDate, nights: Int): LocalDate? =
    generateSequence(maxOf(from, today)) { it.plusDays(1) }
        .takeWhile { !it.plusDays(nights.toLong()).isAfter(window.endExclusive) }
        .firstOrNull { fits(filter, it, it.plusDays(nights.toLong())) }

/** Who is staying on the night of [night]. */
data class NightOccupancy(
    /** Every house when [wholeResortBooking] is set. */
    val takenHouseIds: Set<String>,
    val wholeResortBooking: Booking?,
)

fun Availability.occupancyOn(night: LocalDate): NightOccupancy {
    val staying = bookings.filter { it.coversNight(night) }
    val wholeResort = staying.firstOrNull(Booking::isWholeResort)
    return NightOccupancy(
        takenHouseIds = if (wholeResort != null) houses.map(House::id).toSet() else staying.flatMap(Booking::houseIds).toSet(),
        wholeResortBooking = wholeResort,
    )
}
