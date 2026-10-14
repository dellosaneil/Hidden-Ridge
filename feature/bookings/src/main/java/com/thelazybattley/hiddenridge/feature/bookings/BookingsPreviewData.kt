package com.thelazybattley.hiddenridge.feature.bookings

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.usecase.BookingLog
import java.time.LocalDate

/** The bookings from the design, as seen on Monday 12 October. */
internal object BookingsPreviewData {
    private val today = LocalDate.of(2026, 10, 12)

    val log = BookingLog(
        today = today,
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
            booking("1", "Grace Lim", listOf("c2"), 10, 12, 10, 15),
            booking("2", "Reyes family", listOf("f1"), 10, 9, 10, 12),
            booking("3", "Maria Santos", listOf("c1"), 10, 10, 10, 14),
            booking("4", "Tanaka", listOf("cb"), 10, 14, 10, 17, status = BookingStatus.HOLD),
            booking("5", "Bauer family", listOf("f2"), 10, 16, 10, 20),
            booking("6", "Ferrer", listOf("cc"), 11, 13, 11, 16),
            booking("7", "Ramos wedding", emptyList(), 12, 18, 12, 21, mode = BookingMode.WHOLE_RESORT),
            booking("8", "Lopez", listOf("c3"), 10, 3, 10, 5, status = BookingStatus.CANCELLED),
        ),
    )

    private fun booking(
        id: String,
        guest: String,
        houses: List<String>,
        inMonth: Int,
        inDay: Int,
        outMonth: Int,
        outDay: Int,
        status: BookingStatus = BookingStatus.CONFIRMED,
        mode: BookingMode = BookingMode.INDIVIDUAL,
    ) = Booking(
        id = id,
        guestName = guest,
        houseIds = houses,
        checkIn = LocalDate.of(2026, inMonth, inDay),
        checkOut = LocalDate.of(2026, outMonth, outDay),
        mode = mode,
        status = status,
        holdUntil = null,
        guestCount = 2,
        phone = "",
        note = "",
    )
}
