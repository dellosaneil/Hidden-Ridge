package com.thelazybattley.hiddenridge.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "house_types")
data class HouseTypeEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "plural_name") val pluralName: String,
    /** A `HouseIcon` name from the domain layer. */
    val icon: String,
    val sleeps: Int,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)
