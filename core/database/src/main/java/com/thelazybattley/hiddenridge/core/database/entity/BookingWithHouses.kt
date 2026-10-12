package com.thelazybattley.hiddenridge.core.database.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BookingWithHouses(
    @Embedded val booking: BookingEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BookingHouseCrossRef::class,
            parentColumn = "booking_id",
            entityColumn = "house_id",
        ),
    )
    val houses: List<HouseEntity>,
)
