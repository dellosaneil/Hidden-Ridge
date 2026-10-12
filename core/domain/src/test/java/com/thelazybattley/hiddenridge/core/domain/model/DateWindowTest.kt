package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DateWindowTest {

    @Test
    fun `timeline window starts on the first of last month`() {
        val window = DateWindow.timelineAround(LocalDate.of(2026, 10, 12))

        assertEquals(LocalDate.of(2026, 9, 1), window.start)
        assertEquals(LocalDate.of(2028, 12, 1), window.endExclusive)
    }

    @Test
    fun `index and date convert both ways`() {
        val window = DateWindow.timelineAround(LocalDate.of(2026, 10, 12))
        val today = LocalDate.of(2026, 10, 12)

        assertEquals(41, window.indexOf(today))
        assertEquals(today, window.dateAt(41))
        assertEquals(-1, window.indexOf(LocalDate.of(2026, 8, 31)))
    }
}
