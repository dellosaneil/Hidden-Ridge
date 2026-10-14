package com.thelazybattley.hiddenridge.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.thelazybattley.hiddenridge.core.database.entity.BookingEntity
import com.thelazybattley.hiddenridge.core.database.entity.BookingHouseCrossRef
import com.thelazybattley.hiddenridge.core.database.entity.BookingWithHouses
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    /** Bookings that are not cancelled and have at least one night between [from] and [to] (exclusive). */
    @Transaction
    @Query(
        """
        SELECT * FROM bookings
        WHERE status != '${BookingEntity.STATUS_CANCELLED}'
          AND check_in < :to
          AND check_out > :from
        ORDER BY check_in
        """,
    )
    fun observeActiveBetween(from: LocalDate, to: LocalDate): Flow<List<BookingWithHouses>>

    @Transaction
    @Query("SELECT * FROM bookings WHERE id = :id")
    fun observeById(id: String): Flow<BookingWithHouses?>

    @Query("UPDATE bookings SET status = :status, hold_until = :holdUntil WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, holdUntil: LocalDate?)

    @Query("UPDATE bookings SET checked_in = :checkedIn WHERE id = :id")
    suspend fun updateCheckedIn(id: String, checkedIn: Boolean)

    @Query("UPDATE bookings SET checked_out = :checkedOut WHERE id = :id")
    suspend fun updateCheckedOut(id: String, checkedOut: Boolean)

    /** Also removes the booking's house links, through the foreign key cascade. */
    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookings(bookings: List<BookingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookingHouses(bookingHouses: List<BookingHouseCrossRef>)

    @Query("DELETE FROM booking_houses WHERE booking_id = :bookingId")
    suspend fun deleteBookingHouses(bookingId: String)

    /** An update rather than a replace, so the row isn't deleted and its house links don't cascade away. */
    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Transaction
    suspend fun updateWithHouses(booking: BookingEntity, bookingHouses: List<BookingHouseCrossRef>) {
        updateBooking(booking)
        deleteBookingHouses(booking.id)
        upsertBookingHouses(bookingHouses)
    }

    @Transaction
    suspend fun upsertAll(bookings: List<BookingEntity>, bookingHouses: List<BookingHouseCrossRef>) {
        upsertBookings(bookings)
        upsertBookingHouses(bookingHouses)
    }
}
