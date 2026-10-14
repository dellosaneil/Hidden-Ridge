package com.thelazybattley.hiddenridge.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "bookings",
    indices = [Index("check_in"), Index("check_out")],
)
data class BookingEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "guest_name") val guestName: String,
    /** A `BookingMode` name from the domain layer. */
    val mode: String,
    /** A `BookingStatus` name from the domain layer. */
    val status: String,
    @ColumnInfo(name = "check_in") val checkIn: LocalDate,
    @ColumnInfo(name = "check_out") val checkOut: LocalDate,
    @ColumnInfo(name = "hold_until") val holdUntil: LocalDate?,
    @ColumnInfo(name = "guest_count") val guestCount: Int,
    val phone: String,
    val note: String,
    @ColumnInfo(name = "checked_in", defaultValue = "0") val checkedIn: Boolean = false,
    @ColumnInfo(name = "checked_out", defaultValue = "0") val checkedOut: Boolean = false,
) {
    companion object {
        /** Must match `BookingStatus.CANCELLED.name`; used inside queries, so it has to be a constant. */
        const val STATUS_CANCELLED = "CANCELLED"
    }
}
