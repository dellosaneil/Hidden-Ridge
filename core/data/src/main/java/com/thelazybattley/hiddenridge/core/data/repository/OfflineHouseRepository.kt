package com.thelazybattley.hiddenridge.core.data.repository

import com.thelazybattley.hiddenridge.core.data.mapper.toDomain
import com.thelazybattley.hiddenridge.core.data.mapper.toEntity
import com.thelazybattley.hiddenridge.core.database.dao.HouseDao
import com.thelazybattley.hiddenridge.core.database.entity.HouseTypeEntity
import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.repository.HouseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

internal class OfflineHouseRepository @Inject constructor(
    private val houseDao: HouseDao,
) : HouseRepository {

    /** Grouped by type in the types' own order, so a house added later still lists with its type. */
    override fun observeHouses(): Flow<List<House>> =
        combine(houseDao.observeAll(), observeHouseTypes()) { houses, types ->
            val typesById = types.associateBy(HouseType::id)
            val typeOrder = types.withIndex().associate { (index, type) -> type.id to index }
            houses
                .mapNotNull { house -> typesById[house.type]?.let(house::toDomain) }
                .sortedBy { typeOrder.getValue(it.type.id) }
        }

    override fun observeHouseTypes(): Flow<List<HouseType>> =
        houseDao.observeTypes().map { types -> types.map(HouseTypeEntity::toDomain) }

    override suspend fun addHouse(house: House) {
        // The DAO places both at the end; the sort orders passed here are replaced.
        houseDao.insertAtEnd(house = house.toEntity(sortOrder = 0), type = house.type.toEntity(sortOrder = 0))
    }
}
