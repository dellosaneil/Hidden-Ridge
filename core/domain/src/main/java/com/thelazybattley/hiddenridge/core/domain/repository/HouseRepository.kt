package com.thelazybattley.hiddenridge.core.domain.repository

import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import kotlinx.coroutines.flow.Flow

interface HouseRepository {
    /** Every house, in the order staff list them. */
    fun observeHouses(): Flow<List<House>>

    /** Every house type, in the order they were added. */
    fun observeHouseTypes(): Flow<List<HouseType>>

    /** Adds [house] after the others, and its type too when the type is new. */
    suspend fun addHouse(house: House)
}
