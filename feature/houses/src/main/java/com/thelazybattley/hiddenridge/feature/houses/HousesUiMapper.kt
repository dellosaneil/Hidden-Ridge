package com.thelazybattley.hiddenridge.feature.houses

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.HouseDay
import com.thelazybattley.hiddenridge.core.domain.model.HouseRoster
import com.thelazybattley.hiddenridge.core.domain.model.StayPhase
import com.thelazybattley.hiddenridge.core.domain.model.stayPhaseOn
import com.thelazybattley.hiddenridge.core.domain.model.types
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

private const val DAYS_PER_WEEK = 7L

internal fun HouseRoster.toUi(filter: HousesFilter, firstDayOfWeek: DayOfWeek): HousesUi {
    val weekStart = today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    val week = (0 until DAYS_PER_WEEK).map { weekStart.plusDays(it) }
    val weekEnd = weekStart.plusDays(DAYS_PER_WEEK)
    val all = houses.map { it to it.toRow(today, week, weekEnd, houseCount = houses.size) }
    return HousesUi(
        today = today,
        houseCount = houses.size,
        occupiedTonight = houses.count { it.tonight != null },
        arrivingToday = all.count { it.second.status == HouseStatus.ARRIVING },
        leavingToday = all.count { it.second.status == HouseStatus.LEAVING },
        holds = houses.flatMap(HouseDay::stays).filter { it.isOnHold && it.overlaps(weekStart, weekEnd) }.distinctBy(Booking::id).size,
        week = week,
        filter = filter,
        filterCounts = HousesFilter.entries.associateWith { f -> all.count { it.second.status in f.statuses } },
        rows = houses.map(HouseDay::house).types().flatMap { type ->
            val shown = all.filter { (day, row) -> day.house.type.id == type.id && row.status in filter.statuses }
            if (shown.isEmpty()) {
                emptyList()
            } else {
                listOf(HousesRow.Group(type)) +
                    shown.mapIndexed { index, (_, row) -> HousesRow.House(row, isLastInGroup = index == shown.lastIndex) }
            }
        },
    )
}

private val HousesFilter.statuses: Set<HouseStatus>
    get() = when (this) {
        HousesFilter.ALL -> HouseStatus.entries.toSet()
        HousesFilter.FREE -> setOf(HouseStatus.FREE, HouseStatus.LEAVING)
        HousesFilter.OCCUPIED -> setOf(HouseStatus.OCCUPIED, HouseStatus.ARRIVING)
        HousesFilter.MOVING -> setOf(HouseStatus.ARRIVING, HouseStatus.LEAVING)
    }

private fun HouseDay.toRow(today: LocalDate, week: List<LocalDate>, weekEnd: LocalDate, houseCount: Int): HouseRowUi {
    val tonight = tonight
    val departing = departing
    val (status, summary) = when {
        tonight != null -> when (val phase = tonight.stayPhaseOn(today)) {
            is StayPhase.InHouse -> HouseStatus.OCCUPIED to
                HouseSummary.InHouse(tonight.guestName, phase.night, phase.nights, tonight.checkOut)
            else -> if (tonight.checkedIn) {
                HouseStatus.OCCUPIED to
                    HouseSummary.InHouse(
                        guestName = tonight.guestName,
                        night = 1,
                        nights = tonight.nights,
                        checkOut = tonight.checkOut,
                        justCheckedIn = true,
                    )
            } else {
                HouseStatus.ARRIVING to HouseSummary.Arriving(tonight.guestName, tonight.nights)
            }
        }
        departing != null && departing.checkedOut -> HouseStatus.FREE to HouseSummary.CheckedOut(departing.guestName)
        departing != null -> HouseStatus.LEAVING to HouseSummary.Leaving(departing.guestName)
        !house.isOpenOn(today) -> HouseStatus.FREE to HouseSummary.OpensOn(checkNotNull(house.bookableFrom))
        else -> HouseStatus.FREE to freeSummary(weekEnd)
    }
    val bookingHouses: (Booking) -> Int = { if (it.isWholeResort) houseCount else it.houseIds.size }
    return HouseRowUi(
        houseId = house.id,
        name = house.name,
        icon = house.type.icon,
        status = status,
        summary = summary,
        nights = week.map { night ->
            val stay = stayOn(night)
            when {
                stay == null && !house.isOpenOn(night) -> NightState.CLOSED
                stay == null -> NightState.FREE
                stay.isOnHold -> NightState.HOLD
                else -> NightState.BOOKED
            }
        },
        bookingId = tonight?.id ?: departing?.takeUnless(Booking::checkedOut)?.id,
        action = when (status) {
            HouseStatus.ARRIVING -> tonight?.let { HouseAction.CheckIn(it.id, it.guestName, house.name, bookingHouses(it)) }
            HouseStatus.LEAVING -> departing?.let { HouseAction.CheckOut(it.id, it.guestName, house.name, bookingHouses(it)) }
            else -> null
        },
        opensOn = house.bookableFrom?.takeUnless { house.isOpenOn(today) },
    )
}

private fun HouseDay.freeSummary(weekEnd: LocalDate): HouseSummary {
    val next = next?.takeIf { it.checkIn.isBefore(weekEnd) } ?: return HouseSummary.FreeAllWeek
    return HouseSummary.FreeUntil(until = next.checkIn, guestName = next.guestName, isHold = next.isOnHold)
}
