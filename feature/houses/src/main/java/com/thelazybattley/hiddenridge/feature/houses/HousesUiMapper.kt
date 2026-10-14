package com.thelazybattley.hiddenridge.feature.houses

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.HouseDay
import com.thelazybattley.hiddenridge.core.domain.model.HouseRoster
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.model.StayPhase
import com.thelazybattley.hiddenridge.core.domain.model.stayPhaseOn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

private const val DAYS_PER_WEEK = 7L

internal fun HouseRoster.toUi(filter: HousesFilter, firstDayOfWeek: DayOfWeek): HousesUi {
    val weekStart = today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    val week = (0 until DAYS_PER_WEEK).map { weekStart.plusDays(it) }
    val weekEnd = weekStart.plusDays(DAYS_PER_WEEK)
    val all = houses.map { it to it.toRow(today, week, weekEnd) }
    return HousesUi(
        today = today,
        houseCount = houses.size,
        occupiedTonight = houses.count { it.tonight != null },
        arrivingToday = houses.count { it.tonight?.checkIn == today },
        leavingToday = houses.count { it.departing != null },
        holds = houses.flatMap(HouseDay::stays).filter { it.isOnHold && it.overlaps(weekStart, weekEnd) }.distinctBy(Booking::id).size,
        week = week,
        filter = filter,
        filterCounts = HousesFilter.entries.associateWith { f -> all.count { it.second.status in f.statuses } },
        rows = HouseType.entries.flatMap { type ->
            val shown = all.filter { (day, row) -> day.house.type == type && row.status in filter.statuses }
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

private fun HouseDay.toRow(today: LocalDate, week: List<LocalDate>, weekEnd: LocalDate): HouseRowUi {
    val tonight = tonight
    val departing = departing
    val (status, summary) = when {
        tonight != null -> when (val phase = tonight.stayPhaseOn(today)) {
            is StayPhase.InHouse -> HouseStatus.OCCUPIED to
                HouseSummary.InHouse(tonight.guestName, phase.night, phase.nights, tonight.checkOut)
            else -> HouseStatus.ARRIVING to HouseSummary.Arriving(tonight.guestName, tonight.nights)
        }
        departing != null -> HouseStatus.LEAVING to HouseSummary.Leaving(departing.guestName)
        else -> HouseStatus.FREE to freeSummary(weekEnd)
    }
    return HouseRowUi(
        houseId = house.id,
        name = house.name,
        status = status,
        summary = summary,
        nights = week.map { night ->
            val stay = stayOn(night)
            when {
                stay == null -> NightState.FREE
                stay.isOnHold -> NightState.HOLD
                else -> NightState.BOOKED
            }
        },
        bookingId = (tonight ?: departing)?.id,
    )
}

private fun HouseDay.freeSummary(weekEnd: LocalDate): HouseSummary {
    val next = next?.takeIf { it.checkIn.isBefore(weekEnd) } ?: return HouseSummary.FreeAllWeek
    return HouseSummary.FreeUntil(until = next.checkIn, guestName = next.guestName, isHold = next.isOnHold)
}
