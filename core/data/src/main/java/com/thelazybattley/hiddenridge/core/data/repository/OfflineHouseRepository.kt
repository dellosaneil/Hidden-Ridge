package com.thelazybattley.hiddenridge.core.data.repository

import com.thelazybattley.hiddenridge.core.data.mapper.toDomain
import com.thelazybattley.hiddenridge.core.database.dao.HouseDao
import com.thelazybattley.hiddenridge.core.database.entity.HouseEntity
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.repository.HouseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class OfflineHouseRepository @Inject constructor(
    private val houseDao: HouseDao,
) : HouseRepository {

    override fun observeHouses(): Flow<List<House>> =
        houseDao.observeAll().map { houses -> houses.map(HouseEntity::toDomain) }
}
