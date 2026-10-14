package com.thelazybattley.hiddenridge.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.thelazybattley.hiddenridge.core.database.entity.HouseEntity
import com.thelazybattley.hiddenridge.core.database.entity.HouseTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseDao {
    @Query("SELECT * FROM houses ORDER BY sort_order")
    fun observeAll(): Flow<List<HouseEntity>>

    @Query("SELECT * FROM house_types ORDER BY sort_order")
    fun observeTypes(): Flow<List<HouseTypeEntity>>

    @Query("SELECT COUNT(*) FROM houses")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(houses: List<HouseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTypes(types: List<HouseTypeEntity>)

    /** Keeps an existing type as it is. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTypeIfAbsent(type: HouseTypeEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(house: HouseEntity)

    @Query("SELECT COALESCE(MAX(sort_order), -1) FROM houses")
    suspend fun lastSortOrder(): Int

    @Query("SELECT COALESCE(MAX(sort_order), -1) FROM house_types")
    suspend fun lastTypeSortOrder(): Int

    /** Adds [house] after every other house, and [type] after every other type unless it's already stored. */
    @Transaction
    suspend fun insertAtEnd(house: HouseEntity, type: HouseTypeEntity) {
        insertTypeIfAbsent(type.copy(sortOrder = lastTypeSortOrder() + 1))
        insert(house.copy(sortOrder = lastSortOrder() + 1))
    }
}
