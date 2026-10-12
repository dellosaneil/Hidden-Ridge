package com.thelazybattley.hiddenridge.core.database

import androidx.room.TypeConverter
import java.time.LocalDate

/** Stores dates as epoch days so range queries compare plain integers. */
internal class DateConverters {
    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromEpochDay(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)
}
