package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class BookingMode {
    /** One or more specific houses. */
    INDIVIDUAL,

    /** One group takes every house for the stay. */
    WHOLE_RESORT,
}

enum class BookingStatus {
    CONFIRMED,

    /** Reserved for the guest but not confirmed yet; released after [Booking.holdUntil]. */
    HOLD,

    /** Kept for history only; no longer occupies any house. */
    CANCELLED,
}

data class Booking(
    val id: String,
    val guestName: String,
    /** The houses this booking occupies. Empty for [BookingMode.WHOLE_RESORT], which blocks every house. */
    val houseIds: List<String>,
    val checkIn: LocalDate,
    /** The morning the guest leaves; the night before it is the last night stayed. */
    val checkOut: LocalDate,
    val mode: BookingMode,
    val status: BookingStatus,
    val holdUntil: LocalDate?,
    val guestCount: Int,
    val phone: String,
    val note: String,
) {
    init {
        require(checkOut.isAfter(checkIn)) { "Check-out ($checkOut) must be after check-in ($checkIn)" }
    }

    val nights: Int get() = ChronoUnit.DAYS.between(checkIn, checkOut).toInt()

    val isWholeResort: Boolean get() = mode == BookingMode.WHOLE_RESORT

    val isOnHold: Boolean get() = status == BookingStatus.HOLD

    /** A single guest or group spread across several specific houses. */
    val isGroup: Boolean get() = !isWholeResort && houseIds.size > 1

    /** Whether any night of this stay falls between [from] and [until] (exclusive). */
    fun overlaps(from: LocalDate, until: LocalDate): Boolean = checkIn.isBefore(until) && from.isBefore(checkOut)

    /** Whether the guest sleeps here on the night of [night]. */
    fun coversNight(night: LocalDate): Boolean = !night.isBefore(checkIn) && night.isBefore(checkOut)
}
