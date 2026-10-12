package com.thelazybattley.hiddenridge.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thelazybattley.hiddenridge.core.database.entity.HouseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseDao {
    @Query("SELECT * FROM houses ORDER BY sort_order")
    fun observeAll(): Flow<List<HouseEntity>>

    @Query("SELECT COUNT(*) FROM houses")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(houses: List<HouseEntity>)
}
