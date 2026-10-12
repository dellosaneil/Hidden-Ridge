package com.thelazybattley.hiddenridge.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Which houses an individual or group booking occupies. Whole-resort bookings have no rows here. */
@Entity(
    tableName = "booking_houses",
    primaryKeys = ["booking_id", "house_id"],
    foreignKeys = [
        ForeignKey(
            entity = BookingEntity::class,
            parentColumns = ["id"],
            childColumns = ["booking_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = HouseEntity::class,
            parentColumns = ["id"],
            childColumns = ["house_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("house_id")],
)
data class BookingHouseCrossRef(
    @ColumnInfo(name = "booking_id") val bookingId: String,
    @ColumnInfo(name = "house_id") val houseId: String,
)
