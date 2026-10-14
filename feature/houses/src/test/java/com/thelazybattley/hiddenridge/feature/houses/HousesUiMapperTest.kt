package com.thelazybattley.hiddenridge.feature.houses

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.model.houseRoster
import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class HousesUiMapperTest {

    /** A Monday. */
    private val today = LocalDate.of(2026, 10, 12)
    private val houses = listOf(
        House("f1", "Ridge House", HouseType.FAMILY_HOUSE),
        House("c1", "Cabin 1", HouseType.CABIN),
        House("c2", "Cabin 2", HouseType.CABIN),
        House("ca", "Casita A", HouseType.CASITA),
    )

    @Test
    fun `houses are grouped under a header per type in type order`() {
        val ui = ui()

        assertEquals(
            listOf("group:CABIN", "c1", "c2", "group:CASITA", "ca", "group:FAMILY_HOUSE", "f1"),
            ui.rows.map { row ->
                when (row) {
                    is HousesRow.Group -> "group:${row.type}"
                    is HousesRow.House -> row.house.houseId
                }
            },
        )
        assertEquals(listOf(false, true, true, true), ui.rows.filterIsInstance<HousesRow.House>().map(HousesRow.House::isLastInGroup))
    }

    @Test
    fun `status and summary say who is in the house tonight`() {
        val ui = ui(
            stay("in", listOf("c1"), from = -2, to = 2),
            stay("arriving", listOf("c2"), from = 0, to = 3),
            stay("out", listOf("ca"), from = -3, to = 0),
        )

        val c1 = ui.house("c1")
        assertEquals(HouseStatus.OCCUPIED, c1.status)
        assertEquals(HouseSummary.InHouse("Guest in", night = 3, nights = 4, checkOut = today.plusDays(2)), c1.summary)
        assertEquals("in", c1.bookingId)
        assertEquals(HouseStatus.ARRIVING, ui.house("c2").status)
        assertEquals(HouseSummary.Arriving("Guest arriving", nights = 3), ui.house("c2").summary)
        assertEquals(HouseStatus.LEAVING, ui.house("ca").status)
        assertEquals("out", ui.house("ca").bookingId)
        assertEquals(HouseStatus.FREE, ui.house("f1").status)
        assertEquals(null, ui.house("f1").bookingId)
    }

    @Test
    fun `a free house names its next stay only within the week`() {
        val ui = ui(
            stay("soon", listOf("c1"), from = 3, to = 5),
            stay("hold", listOf("c2"), from = 2, to = 4, status = BookingStatus.HOLD),
            stay("later", listOf("ca"), from = 9, to = 11),
        )

        assertEquals(HouseSummary.FreeUntil(today.plusDays(3), "Guest soon", isHold = false), ui.house("c1").summary)
        assertEquals(HouseSummary.FreeUntil(today.plusDays(2), "Guest hold", isHold = true), ui.house("c2").summary)
        assertEquals(HouseSummary.FreeAllWeek, ui.house("ca").summary)
    }

    @Test
    fun `the strip covers the calendar week holding today`() {
        val ui = ui(
            stay("booked", listOf("c1"), from = -2, to = 1),
            stay("hold", listOf("c1"), from = 3, to = 5, status = BookingStatus.HOLD),
        )

        assertEquals(today.minusDays(1), ui.week.first())
        assertEquals(1, ui.todayIndex)
        val f = NightState.FREE
        val b = NightState.BOOKED
        val h = NightState.HOLD
        assertEquals(listOf(b, b, f, f, h, h, f), ui.house("c1").nights)
        assertEquals(1, ui.holds)
    }

    @Test
    fun `filters keep matching houses and count every option`() {
        val bookings = arrayOf(
            stay("in", listOf("c1"), from = -2, to = 2),
            stay("arriving", listOf("c2"), from = 0, to = 3),
            stay("out", listOf("ca"), from = -3, to = 0),
        )

        val free = ui(*bookings, filter = HousesFilter.FREE)
        assertEquals(listOf("ca", "f1"), free.rows.filterIsInstance<HousesRow.House>().map { it.house.houseId })
        assertEquals(
            mapOf(HousesFilter.ALL to 4, HousesFilter.FREE to 2, HousesFilter.OCCUPIED to 2, HousesFilter.MOVING to 2),
            free.filterCounts,
        )
        assertEquals(2, free.occupiedTonight)
        assertEquals(1, free.arrivingToday)
        assertEquals(1, free.leavingToday)
    }

    @Test
    fun `a whole resort stay occupies every house`() {
        val ui = ui(stay("resort", emptyList(), from = -1, to = 2, mode = BookingMode.WHOLE_RESORT))

        assertEquals(4, ui.occupiedTonight)
        assertEquals(setOf(HouseStatus.OCCUPIED), ui.rows.filterIsInstance<HousesRow.House>().map { it.house.status }.toSet())
    }

    private fun ui(vararg bookings: Booking, filter: HousesFilter = HousesFilter.ALL): HousesUi =
        houseRoster(today, houses, bookings.toList()).toUi(filter, DayOfWeek.SUNDAY)

    private fun HousesUi.house(id: String): HouseRowUi =
        rows.filterIsInstance<HousesRow.House>().map(HousesRow.House::house).single { it.houseId == id }

    private fun stay(
        id: String,
        houseIds: List<String>,
        from: Long,
        to: Long,
        mode: BookingMode = BookingMode.INDIVIDUAL,
        status: BookingStatus = BookingStatus.CONFIRMED,
    ) = Booking(
        id = id,
        guestName = "Guest $id",
        houseIds = houseIds,
        checkIn = today.plusDays(from),
        checkOut = today.plusDays(to),
        mode = mode,
        status = status,
        holdUntil = null,
        guestCount = 2,
        phone = "",
        note = "",
    )
}
