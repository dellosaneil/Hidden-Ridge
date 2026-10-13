package com.thelazybattley.hiddenridge.feature.timeline

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.model.Timeline
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineUiMapperTest {

    private val today = LocalDate.of(2026, 10, 12)
    private val houses = listOf(
        House("ca", "Casita A", HouseType.CASITA),
        House("c1", "Cabin 1", HouseType.CABIN),
        House("c2", "Cabin 2", HouseType.CABIN),
    )

    @Test
    fun `houses are grouped by type in type order`() {
        val ui = timeline(bookings = emptyList()).toUi()

        assertEquals(
            listOf(
                TimelineRow.Group(HouseType.CABIN),
                TimelineRow.House("c1", "Cabin 1"),
                TimelineRow.House("c2", "Cabin 2"),
                TimelineRow.Group(HouseType.CASITA),
                TimelineRow.House("ca", "Casita A"),
            ),
            ui.rows,
        )
    }

    @Test
    fun `a group booking gets one bar per house on that house's row`() {
        val group = booking(houseIds = listOf("c2", "ca"), checkIn = today, checkOut = today.plusDays(3))

        val bars = timeline(bookings = listOf(group)).toUi().bookingBars

        assertEquals(listOf(2, 4), bars.map(BookingBar::rowIndex))
        assertTrue(bars.all { it.isGroup && it.nights == 3 && it.startIndex == 41 })
    }

    @Test
    fun `whole resort bookings become blocks, not bars`() {
        val wedding = booking(
            houseIds = emptyList(),
            checkIn = today.plusDays(10),
            checkOut = today.plusDays(13),
            mode = BookingMode.WHOLE_RESORT,
            status = BookingStatus.HOLD,
        )

        val ui = timeline(bookings = listOf(wedding)).toUi()

        assertTrue(ui.bookingBars.isEmpty())
        assertEquals(51, ui.resortBlocks.single().startIndex)
        assertTrue(ui.resortBlocks.single().isOnHold)
    }

    @Test
    fun `months cover the window and today resolves to its month`() {
        val ui = timeline(bookings = emptyList()).toUi()

        assertEquals(YearMonth.of(2026, 9), ui.months.first().yearMonth)
        assertEquals(ui.dayCount, ui.months.sumOf(TimelineMonth::dayCount))
        assertEquals(YearMonth.of(2026, 10), ui.months[ui.monthIndexAt(ui.todayIndex)].yearMonth)
    }

    @Test
    fun `each month counts the house-nights booked inside it`() {
        // Two of three houses for the last 2 nights of October, then 1 night in November.
        val stay = booking(houseIds = listOf("c1", "c2"), checkIn = LocalDate.of(2026, 10, 30), checkOut = LocalDate.of(2026, 11, 2))
        val wedding = booking(
            houseIds = emptyList(),
            checkIn = LocalDate.of(2026, 12, 18),
            checkOut = LocalDate.of(2026, 12, 21),
            mode = BookingMode.WHOLE_RESORT,
        ).copy(id = "w1")

        val months = timeline(bookings = listOf(stay, wedding)).toUi().months.associateBy { it.yearMonth }

        assertEquals(4f / (3 * 31), months.getValue(YearMonth.of(2026, 10)).bookedFraction, 0.0001f)
        assertEquals(2f / (3 * 30), months.getValue(YearMonth.of(2026, 11)).bookedFraction, 0.0001f)
        assertEquals(9f / (3 * 31), months.getValue(YearMonth.of(2026, 12)).bookedFraction, 0.0001f)
        assertTrue(months.getValue(YearMonth.of(2026, 12)).hasWholeResort)
        assertFalse(months.getValue(YearMonth.of(2026, 11)).hasWholeResort)
    }

    @Test
    fun `a picked-up booking that hasn't moved is lifted with no ghost and nothing to save`() {
        val stay = booking(houseIds = listOf("c1"), checkIn = today.plusDays(2), checkOut = today.plusDays(4))
        val timeline = timeline(bookings = listOf(stay))
        val draft = MoveDraft(bookingId = "b1")

        val ui = timeline.toUi(draft)
        val move = timeline.moveUi(draft, isSaving = false)!!

        assertEquals(MoveState.LIFTED, ui.bookingBars.single().moveState)
        assertTrue(ui.ghosts.isEmpty())
        assertEquals(MoveStatus.NotMoved, move.status)
        assertFalse(move.canSave)
    }

    @Test
    fun `dragging to a free house and day leaves a ghost and is ready to save`() {
        val stay = booking(houseIds = listOf("c1"), checkIn = today.plusDays(2), checkOut = today.plusDays(4))
        val timeline = timeline(bookings = listOf(stay))
        val draft = MoveDraft(bookingId = "b1", dayShift = 1, houseId = "c2")

        val ui = timeline.toUi(draft)
        val move = timeline.moveUi(draft, isSaving = false)!!

        val bar = ui.bookingBars.single()
        assertEquals(2, bar.rowIndex)
        assertEquals(44, bar.startIndex)
        assertEquals(listOf(GhostBar(rowIndex = 1, startIndex = 43, nights = 2)), ui.ghosts)
        assertEquals(listOf("Cabin 2"), move.houseNames)
        assertEquals(MoveStatus.Ready, move.status)
        assertTrue(move.canSave)
    }

    @Test
    fun `landing on another booking shows the overlap and blocks saving`() {
        val stay = booking(houseIds = listOf("c1"), checkIn = today.plusDays(2), checkOut = today.plusDays(4))
        val other = booking(houseIds = listOf("c2"), checkIn = today.plusDays(3), checkOut = today.plusDays(5)).copy(id = "b2", guestName = "Other")
        val timeline = timeline(bookings = listOf(stay, other))
        val draft = MoveDraft(bookingId = "b1", houseId = "c2")

        val ui = timeline.toUi(draft)
        val move = timeline.moveUi(draft, isSaving = false)!!

        assertEquals(MoveState.LIFTED_CONFLICT, ui.bookingBars.first { it.bookingId == "b1" }.moveState)
        assertEquals("Other", (move.status as MoveStatus.Overlaps).guestName)
        assertFalse(move.canSave)
        assertFalse(ui.bookingBars.first { it.bookingId == "b2" }.canMove)
    }

    @Test
    fun `a group keeps its houses and only offers date arrows`() {
        val group = booking(houseIds = listOf("c1", "c2"), checkIn = today.plusDays(2), checkOut = today.plusDays(4))
        val timeline = timeline(bookings = listOf(group))
        val draft = MoveDraft(bookingId = "b1", dayShift = 1, houseId = "ca")

        val move = timeline.moveUi(draft, isSaving = false)!!

        assertEquals(listOf("Cabin 1", "Cabin 2"), move.houseNames)
        assertFalse(move.canChangeHouse)
        assertNull(move.houseRowIndex)
    }

    @Test
    fun `a stay already under way can't go earlier`() {
        val inHouse = booking(houseIds = listOf("c1"), checkIn = today.minusDays(1), checkOut = today.plusDays(2))

        val move = timeline(bookings = listOf(inHouse)).moveUi(MoveDraft(bookingId = "b1"), isSaving = false)!!

        assertFalse(move.canGoEarlier)
        assertTrue(move.canGoLater)
    }

    private fun timeline(bookings: List<Booking>) = Timeline(
        today = today,
        window = DateWindow.timelineAround(today),
        houses = houses,
        bookings = bookings,
    )

    private fun booking(
        houseIds: List<String>,
        checkIn: LocalDate,
        checkOut: LocalDate,
        mode: BookingMode = BookingMode.INDIVIDUAL,
        status: BookingStatus = BookingStatus.CONFIRMED,
    ) = Booking(
        id = "b1",
        guestName = "Guest",
        houseIds = houseIds,
        checkIn = checkIn,
        checkOut = checkOut,
        mode = mode,
        status = status,
        holdUntil = null,
        guestCount = 2,
        phone = "",
        note = "",
    )
}
