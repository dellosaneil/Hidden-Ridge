package com.thelazybattley.hiddenridge.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thelazybattley.hiddenridge.core.database.dao.BookingDao
import com.thelazybattley.hiddenridge.core.database.dao.HouseDao
import com.thelazybattley.hiddenridge.core.database.entity.BookingEntity
import com.thelazybattley.hiddenridge.core.database.entity.BookingHouseCrossRef
import com.thelazybattley.hiddenridge.core.database.entity.HouseEntity

@Database(
    entities = [HouseEntity::class, BookingEntity::class, BookingHouseCrossRef::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(DateConverters::class)
abstract class HiddenRidgeDatabase : RoomDatabase() {
    abstract fun houseDao(): HouseDao

    abstract fun bookingDao(): BookingDao
}
