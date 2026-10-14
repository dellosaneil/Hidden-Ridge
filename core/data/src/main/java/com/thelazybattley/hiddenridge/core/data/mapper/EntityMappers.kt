package com.thelazybattley.hiddenridge.core.data.mapper

import com.thelazybattley.hiddenridge.core.database.entity.BookingEntity
import com.thelazybattley.hiddenridge.core.database.entity.BookingHouseCrossRef
import com.thelazybattley.hiddenridge.core.database.entity.BookingWithHouses
import com.thelazybattley.hiddenridge.core.database.entity.HouseEntity
import com.thelazybattley.hiddenridge.core.database.entity.HouseTypeEntity
import com.thelazybattley.hiddenridge.core.domain.model.Booking
import com.thelazybattley.hiddenridge.core.domain.model.BookingMode
import com.thelazybattley.hiddenridge.core.domain.model.BookingStatus
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseIcon
import com.thelazybattley.hiddenridge.core.domain.model.HouseType

internal fun HouseTypeEntity.toDomain(): HouseType = HouseType(
    id = id,
    name = name,
    pluralName = pluralName,
    icon = HouseIcon.valueOf(icon),
    sleeps = sleeps,
)

internal fun HouseType.toEntity(sortOrder: Int): HouseTypeEntity = HouseTypeEntity(
    id = id,
    name = name,
    pluralName = pluralName,
    icon = icon.name,
    sleeps = sleeps,
    sortOrder = sortOrder,
)

internal fun HouseEntity.toDomain(type: HouseType): House = House(
    id = id,
    name = name,
    type = type,
    sleeps = sleeps,
    bookableFrom = bookableFrom,
    note = note,
)

internal fun House.toEntity(sortOrder: Int): HouseEntity = HouseEntity(
    id = id,
    name = name,
    type = type.id,
    sortOrder = sortOrder,
    sleeps = sleeps,
    bookableFrom = bookableFrom,
    note = note,
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
    checkedIn = checkedIn,
    checkedOut = checkedOut,
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
    checkedIn = booking.checkedIn,
    checkedOut = booking.checkedOut,
)
