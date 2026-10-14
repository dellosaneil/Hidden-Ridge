package com.thelazybattley.hiddenridge.core.data.sample

import androidx.room.withTransaction
import com.thelazybattley.hiddenridge.core.database.HiddenRidgeDatabase
import com.thelazybattley.hiddenridge.core.domain.time.DateProvider
import javax.inject.Inject

/** Fills an empty database with the resort's houses and the design's sample bookings. */
class SampleDataSeeder @Inject internal constructor(
    private val database: HiddenRidgeDatabase,
    private val dateProvider: DateProvider,
) {
    suspend fun seedIfEmpty() {
        database.withTransaction {
            if (database.houseDao().count() > 0) return@withTransaction
            val today = dateProvider.today()
            database.houseDao().upsertTypes(SampleData.houseTypes)
            database.houseDao().upsertAll(SampleData.houses)
            database.bookingDao().upsertAll(
                bookings = SampleData.bookingEntities(today),
                bookingHouses = SampleData.bookingHouses(),
            )
        }
    }
}
