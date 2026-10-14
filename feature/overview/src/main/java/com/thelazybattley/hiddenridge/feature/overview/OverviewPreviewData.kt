package com.thelazybattley.hiddenridge.feature.overview

import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.usecase.BookingLog
import java.time.LocalDate

/** The October week from the design, as seen on Monday the 12th. */
internal object OverviewPreviewData {
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
            booking("1", "Reyes family", listOf("f1"), in10 = 9, out10 = 12),
            booking("2", "Maria Santos", listOf("c1"), in10 = 10, out10 = 14),
            booking("3", "N. Okafor", listOf("ca"), in10 = 11, out10 = 18),
            booking("4", "Grace Lim", listOf("c2"), in10 = 12, out10 = 15),
            booking("5", "Tanaka", listOf("cb"), in10 = 14, out10 = 17, status = BookingStatus.HOLD),
            booking("6", "Nguyen", listOf("c1"), in10 = 14, out10 = 16),
            booking("7", "Bauer family", listOf("f2"), in10 = 16, out10 = 20),
            booking("8", "Castillo", listOf("c3"), in10 = 17, out10 = 19),
            Booking(
                id = "9", guestName = "Elena Park", houseIds = listOf("ca"),
                checkIn = LocalDate.of(2026, 11, 14), checkOut = LocalDate.of(2026, 11, 17),
                mode = BookingMode.INDIVIDUAL, status = BookingStatus.HOLD, holdUntil = LocalDate.of(2026, 10, 15),
                guestCount = 2, phone = "", note = "",
            ),
            Booking(
                id = "10", guestName = "Ramos wedding", houseIds = emptyList(),
                checkIn = LocalDate.of(2026, 12, 18), checkOut = LocalDate.of(2026, 12, 21),
                mode = BookingMode.WHOLE_RESORT, status = BookingStatus.CONFIRMED, holdUntil = null,
                guestCount = 38, phone = "", note = "",
            ),
        ),
    )

    private fun booking(id: String, guest: String, houses: List<String>, in10: Int, out10: Int, status: BookingStatus = BookingStatus.CONFIRMED) =
        Booking(
            id = id,
            guestName = guest,
            houseIds = houses,
            checkIn = LocalDate.of(2026, 10, in10),
            checkOut = LocalDate.of(2026, 10, out10),
            mode = BookingMode.INDIVIDUAL,
            status = status,
            holdUntil = null,
            guestCount = 2,
            phone = "",
            note = "",
        )
}
