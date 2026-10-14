package com.thelazybattley.hiddenridge.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "houses")
data class HouseEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** The id of a row in `house_types`. */
    val type: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(defaultValue = "0") val sleeps: Int,
    @ColumnInfo(name = "bookable_from") val bookableFrom: LocalDate? = null,
    @ColumnInfo(defaultValue = "''") val note: String = "",
)
