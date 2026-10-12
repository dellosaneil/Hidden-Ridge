package com.thelazybattley.hiddenridge.feature.timeline

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.DateWindow
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.model.Timeline
import java.time.LocalDate

/** A short week from the design for previews. */
internal object TimelinePreviewData {
    private val today = LocalDate.of(2026, 10, 12)

    val timeline = Timeline(
        today = today,
        window = DateWindow.timelineAround(today),
        houses = listOf(
            House("c1", "Cabin 1", HouseType.CABIN),
            House("c2", "Cabin 2", HouseType.CABIN),
            House("c3", "Cabin 3", HouseType.CABIN),
            House("ca", "Casita A", HouseType.CASITA),
            House("cb", "Casita B", HouseType.CASITA),
            House("cc", "Casita C", HouseType.CASITA),
            House("f1", "Ridge House", HouseType.FAMILY_HOUSE),
            House("f2", "Lake House", HouseType.FAMILY_HOUSE),
        ),
        bookings = listOf(
            booking("1", "Reyes family", listOf("f1"), from = 9, to = 12),
            booking("2", "Maria Santos", listOf("c1"), from = 10, to = 14),
            booking("3", "N. Okafor", listOf("ca"), from = 11, to = 18),
            booking("4", "Grace Lim", listOf("c2"), from = 12, to = 15),
            booking("5", "Tanaka", listOf("cb"), from = 14, to = 17, status = BookingStatus.HOLD),
            booking("6", "Nguyen", listOf("c1"), from = 14, to = 16),
            booking("7", "Bauer family", listOf("f2"), from = 16, to = 20),
            booking("8", "Castillo", listOf("c3"), from = 17, to = 19),
        ),
    )

    private fun booking(
        id: String,
        guest: String,
        houses: List<String>,
        from: Int,
        to: Int,
        status: BookingStatus = BookingStatus.CONFIRMED,
    ) = Booking(
        id = id,
        guestName = guest,
        houseIds = houses,
        checkIn = LocalDate.of(2026, 10, from),
        checkOut = LocalDate.of(2026, 10, to),
        mode = BookingMode.INDIVIDUAL,
        status = status,
        holdUntil = null,
        guestCount = 2,
        phone = "",
        note = "",
    )
}
