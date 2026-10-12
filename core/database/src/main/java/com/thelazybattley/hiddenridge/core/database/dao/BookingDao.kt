package com.thelazybattley.hiddenridge.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookings(bookings: List<BookingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookingHouses(bookingHouses: List<BookingHouseCrossRef>)

    @Transaction
    suspend fun upsertAll(bookings: List<BookingEntity>, bookingHouses: List<BookingHouseCrossRef>) {
        upsertBookings(bookings)
        upsertBookingHouses(bookingHouses)
    }
}
