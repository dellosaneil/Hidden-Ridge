package com.thelazybattley.hiddenridge.core.data.sample

import com.thelazybattley.hiddenridge.core.database.entity.BookingEntity
import com.thelazybattley.hiddenridge.core.database.entity.BookingHouseCrossRef
import com.thelazybattley.hiddenridge.core.database.entity.HouseEntity
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import java.time.LocalDate

/**
 * The houses and sample bookings from the design. Booking dates are stored as offsets from today,
 * so the timeline always opens on the same scenario: guests in-house, arrivals, holds and
 * whole-resort blocks ahead.
 */
internal object SampleData {

    val houses: List<HouseEntity> = listOf(
        Triple("c1", "Cabin 1", HouseType.CABIN),
        Triple("c2", "Cabin 2", HouseType.CABIN),
        Triple("c3", "Cabin 3", HouseType.CABIN),
        Triple("ca", "Casita A", HouseType.CASITA),
        Triple("cb", "Casita B", HouseType.CASITA),
        Triple("cc", "Casita C", HouseType.CASITA),
        Triple("f1", "Ridge House", HouseType.FAMILY_HOUSE),
        Triple("f2", "Lake House", HouseType.FAMILY_HOUSE),
    ).mapIndexed { index, (id, name, type) ->
        HouseEntity(id = id, name = name, type = type.name, sortOrder = index)
    }

    private val bookings: List<SampleBooking> = listOf(
        SampleBooking("Reyes family", listOf("f1"), fromToday = -3, nights = 3, guests = 6, phone = "(555) 201-4410"),
        SampleBooking(
            "Maria Santos", listOf("c1"), fromToday = -2, nights = 4, phone = "(555) 318-2207",
            note = "Anniversary. Asked for a late check-out.",
        ),
        SampleBooking("N. Okafor", listOf("ca"), fromToday = -1, nights = 7, guests = 3, phone = "(555) 402-1189"),
        SampleBooking("Grace Lim", listOf("c2"), fromToday = 0, nights = 3, phone = "(555) 233-9051"),
        SampleBooking(
            "Tanaka", listOf("cb"), fromToday = 2, nights = 3, guests = 4,
            status = BookingStatus.HOLD, holdUntilFromToday = 1, note = "Waiting for the deposit.",
        ),
        SampleBooking("Nguyen", listOf("c1"), fromToday = 2, nights = 2, phone = "(555) 714-3302"),
        SampleBooking("Bauer family", listOf("f2"), fromToday = 4, nights = 4, guests = 5),
        SampleBooking("Castillo", listOf("c3"), fromToday = 5, nights = 2),
        SampleBooking("Patel", listOf("cc"), fromToday = 10, nights = 3, guests = 3),
        SampleBooking(
            "Dela Cruz", listOf("f1"), fromToday = 12, nights = 2, guests = 7,
            status = BookingStatus.HOLD, holdUntilFromToday = 4,
        ),
        SampleBooking(
            "Kim yoga group", listOf("c2", "c3", "ca"), fromToday = 18, nights = 3, guests = 8,
            note = "One group across three houses.",
        ),
        SampleBooking("Herrera", listOf("ca"), fromToday = 25, nights = 3, guests = 4),
        SampleBooking("Ferrer", listOf("cc"), fromToday = 32, nights = 3, guests = 3),
        SampleBooking(
            "Ramos wedding", emptyList(), fromToday = 67, nights = 3, guests = 38, phone = "(555) 640-7723",
            mode = BookingMode.WHOLE_RESORT, note = "Ceremony on the lawn. Caterer arrives on the first day, 9:00.",
        ),
        SampleBooking("Johansson", listOf("f2"), fromToday = 75, nights = 7, guests = 6),
        SampleBooking("Aquino", listOf("c1"), fromToday = 77, nights = 4),
        SampleBooking("Wong", listOf("c3"), fromToday = 124, nights = 2),
        SampleBooking(
            "Mendoza company retreat", emptyList(), fromToday = 151, nights = 3, guests = 30,
            mode = BookingMode.WHOLE_RESORT,
        ),
        SampleBooking("Lopez", listOf("cb"), fromToday = 165, nights = 4, guests = 4),
        SampleBooking(
            "Garcia family reunion", emptyList(), fromToday = 262, nights = 4, guests = 42,
            mode = BookingMode.WHOLE_RESORT, status = BookingStatus.HOLD, holdUntilFromToday = 112,
            note = "Waiting for the deposit.",
        ),
    )

    fun bookingEntities(today: LocalDate): List<BookingEntity> =
        bookings.mapIndexed { index, booking -> booking.toEntity(id = bookingId(index), today = today) }

    fun bookingHouses(): List<BookingHouseCrossRef> =
        bookings.flatMapIndexed { index, booking ->
            booking.houseIds.map { houseId -> BookingHouseCrossRef(bookingId = bookingId(index), houseId = houseId) }
        }

    private fun bookingId(index: Int) = "sample-${index + 1}"

    private data class SampleBooking(
        val guestName: String,
        val houseIds: List<String>,
        val fromToday: Long,
        val nights: Long,
        val guests: Int = 2,
        val phone: String = "",
        val note: String = "",
        val mode: BookingMode = BookingMode.INDIVIDUAL,
        val status: BookingStatus = BookingStatus.CONFIRMED,
        val holdUntilFromToday: Long? = null,
    ) {
        fun toEntity(id: String, today: LocalDate): BookingEntity {
            val checkIn = today.plusDays(fromToday)
            return BookingEntity(
                id = id,
                guestName = guestName,
                mode = mode.name,
                status = status.name,
                checkIn = checkIn,
                checkOut = checkIn.plusDays(nights),
                holdUntil = holdUntilFromToday?.let(today::plusDays),
                guestCount = guests,
                phone = phone,
                note = note,
            )
        }
    }
}
