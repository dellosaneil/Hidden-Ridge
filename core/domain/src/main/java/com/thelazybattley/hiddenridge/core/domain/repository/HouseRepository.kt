package com.thelazybattley.hiddenridge.core.domain.repository

import com.thelazybattley.hiddenridge.core.domain.model.House
import kotlinx.coroutines.flow.Flow

interface HouseRepository {
    /** Every house, in the order staff list them. */
    fun observeHouses(): Flow<List<House>>
}
