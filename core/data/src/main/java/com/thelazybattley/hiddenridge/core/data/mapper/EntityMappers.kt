package com.thelazybattley.hiddenridge.core.data.mapper

import com.thelazybattley.hiddenridge.core.database.entity.BookingEntity
import com.thelazybattley.hiddenridge.core.database.entity.BookingHouseCrossRef
import com.thelazybattley.hiddenridge.core.database.entity.BookingWithHouses
import com.thelazybattley.hiddenridge.core.database.entity.HouseEntity
import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType

internal fun HouseEntity.toDomain(): House = House(
    id = id,
    name = name,
    type = HouseType.valueOf(type),
)

internal fun Booking.toEntity(): BookingEntity = BookingEntity(
    id = id,
    guestName = guestName,
    mode = mode.name,
    status = status.name,
    checkIn = checkIn,
    checkOut = checkOut,
    holdUntil = holdUntil,
    guestCount = guestCount,
    phone = phone,
    note = note,
)

internal fun Booking.toHouseRefs(): List<BookingHouseCrossRef> =
    houseIds.map { houseId -> BookingHouseCrossRef(bookingId = id, houseId = houseId) }

internal fun BookingWithHouses.toDomain(): Booking = Booking(
    id = booking.id,
    guestName = booking.guestName,
    houseIds = houses.sortedBy(HouseEntity::sortOrder).map(HouseEntity::id),
    checkIn = booking.checkIn,
    checkOut = booking.checkOut,
    mode = BookingMode.valueOf(booking.mode),
    status = BookingStatus.valueOf(booking.status),
    holdUntil = booking.holdUntil,
    guestCount = booking.guestCount,
    phone = booking.phone,
    note = booking.note,
)
