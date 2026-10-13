package com.thelazybattley.hiddenridge.feature.bookingdetails

import com.thelazybattley.hiddenridge.core.domain.model.BookingDetails
import com.thelazybattley.hiddenridge.core.domain.model.House

internal fun BookingDetails.toUi(): BookingDetailsUi = BookingDetailsUi(
    guestName = booking.guestName,
    kind = when {
        booking.isWholeResort -> BookingKind.WHOLE_RESORT
        booking.isGroup -> BookingKind.GROUP
        else -> BookingKind.INDIVIDUAL
    },
    status = if (booking.isOnHold) BookingStatusUi.OnHold(until = booking.holdUntil) else BookingStatusUi.Confirmed,
    stayPhase = stayPhase,
    checkIn = booking.checkIn,
    checkOut = booking.checkOut,
    nights = booking.nights,
    houses = when {
        booking.isWholeResort -> HousesUi.WholeResort(houseCount = houses.size, totalSleeps = totalSleeps)
        houses.size == 1 -> HousesUi.Single(name = houses.single().name, sleeps = houses.single().sleeps)
        else -> HousesUi.Several(names = houses.map(House::name), totalSleeps = totalSleeps)
    },
    guestCount = booking.guestCount,
    phone = booking.phone,
    note = booking.note,
)
