package com.thelazybattley.hiddenridge.core.domain.usecase

import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseDraft
import com.thelazybattley.hiddenridge.core.domain.model.HouseProblem
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.model.TypeChoice
import com.thelazybattley.hiddenridge.core.domain.repository.HouseRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Adds a house, and its type when staff named a new one, unless the draft still has problems. */
class AddHouseUseCase @Inject constructor(
    private val houseRepository: HouseRepository,
) {
    sealed interface Result {
        data class Added(val house: House, val isNewType: Boolean) : Result

        data class Invalid(val problems: List<HouseProblem>) : Result
    }

    suspend operator fun invoke(draft: HouseDraft): Result {
        // Check against the stored houses: the form's copy may be a moment out of date.
        val houses = houseRepository.observeHouses().first()
        val types = houseRepository.observeHouseTypes().first()
        val problems = draft.problems(houses, types)
        if (problems.isNotEmpty()) return Result.Invalid(problems)

        val type = when (val choice = draft.type) {
            is TypeChoice.Existing -> choice.type
            is TypeChoice.New -> HouseType(
                id = UUID.randomUUID().toString(),
                name = choice.name.trim(),
                pluralName = HouseType.pluralOf(choice.name),
                icon = choice.icon,
                sleeps = draft.sleeps,
            )
        }
        val house = House(
            id = UUID.randomUUID().toString(),
            name = draft.name.trim(),
            type = type,
            sleeps = draft.sleeps,
            bookableFrom = draft.bookableFrom,
            note = draft.note.trim(),
        )
        houseRepository.addHouse(house)
        return Result.Added(house, isNewType = draft.isNewType)
    }
}
