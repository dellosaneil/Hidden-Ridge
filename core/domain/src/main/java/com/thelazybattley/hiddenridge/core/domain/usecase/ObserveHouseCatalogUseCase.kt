package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.repository.HouseRepository
import com.thelazybattley.hiddenridge.core.domain.time.DateProvider
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Every house and house type, for adding another. */
data class HouseCatalog(
    val today: LocalDate,
    val houses: List<House>,
    val types: List<HouseType>,
)

class ObserveHouseCatalogUseCase @Inject constructor(
    private val houseRepository: HouseRepository,
    private val dateProvider: DateProvider,
) {
    operator fun invoke(): Flow<HouseCatalog> =
        combine(houseRepository.observeHouses(), houseRepository.observeHouseTypes()) { houses, types ->
            HouseCatalog(today = dateProvider.today(), houses = houses, types = types)
        }
}
