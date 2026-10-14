package com.thelazybattley.hiddenridge.feature.addhouse

import androidx.compose.runtime.Immutable
import com.thelazybattley.hiddenridge.core.domain.model.HouseIcon
import com.thelazybattley.hiddenridge.core.domain.model.HouseProblem
import java.time.LocalDate

internal sealed interface AddHouseUiState {
    data object Loading : AddHouseUiState

    data class Editing(val form: AddHouseFormUi) : AddHouseUiState

    /** The house is stored; the sheet closes and confirms it. */
    data class Added(val houseName: String, val newGroupName: String?) : AddHouseUiState
}

internal sealed interface AddHouseAction {
    data class SelectType(val typeId: String) : AddHouseAction

    data object SelectNewType : AddHouseAction

    data class SetNewTypeName(val name: String) : AddHouseAction

    data class SetNewTypeIcon(val icon: HouseIcon) : AddHouseAction

    data class SetName(val name: String) : AddHouseAction

    data object AddSleeps : AddHouseAction

    data object RemoveSleeps : AddHouseAction

    data object OpenDatePicker : AddHouseAction

    data object CloseDatePicker : AddHouseAction

    data class SetBookableFrom(val date: LocalDate) : AddHouseAction

    data class SetNote(val note: String) : AddHouseAction

    data object Save : AddHouseAction
}

@Immutable
internal data class AddHouseFormUi(
    val types: List<TypeCardUi>,
    val isNewType: Boolean,
    val newTypeName: String,
    val newTypeIcon: HouseIcon,
    val name: String,
    val nameHint: NameHint,
    val sleeps: Int,
    val bookableFrom: LocalDate,
    val note: String,
    /** The type's picture, as the house will show in the Houses tab. */
    val icon: HouseIcon,
    /** The group the house joins, such as "Cabins"; empty until a new type has a name. */
    val groupName: String,
    val houseCount: Int,
    val today: LocalDate,
    val problems: List<HouseProblem>,
    val showProblems: Boolean,
    val isDatePickerOpen: Boolean,
    val isSaving: Boolean,
) {
    val canSave: Boolean get() = !isSaving

    fun shows(problem: HouseProblem): Boolean = showProblems && problem in problems
}

@Immutable
internal data class TypeCardUi(
    val id: String,
    val name: String,
    val icon: HouseIcon,
    val sleeps: Int,
    val houseCount: Int,
    val isSelected: Boolean,
)

/** The line under the name field saying where the name came from. */
@Immutable
internal sealed interface NameHint {
    /** Filled in from the pattern the other houses of [groupName] use. */
    data class SuggestedFrom(val groupName: String) : NameHint

    data object NumberedFromType : NameHint

    data object None : NameHint
}
