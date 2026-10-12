package com.thelazybattley.hiddenridge.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "houses")
data class HouseEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** A `HouseType` name from the domain layer. */
    val type: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)
