package com.thelazybattley.hiddenridge.feature.addhouse

import com.thelazybattley.hiddenridge.core.domain.model.HouseDraft
import com.thelazybattley.hiddenridge.core.domain.model.HouseIcon
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.model.TypeChoice
import com.thelazybattley.hiddenridge.core.domain.model.firstHouseName
import com.thelazybattley.hiddenridge.core.domain.model.suggestHouseName
import com.thelazybattley.hiddenridge.core.domain.usecase.HouseCatalog
import java.time.LocalDate

/** The icons a new type can use, in the order the design offers them. */
internal val NewTypeIcons = listOf(HouseIcon.TREE, HouseIcon.CABIN, HouseIcon.COTTAGE, HouseIcon.TENT, HouseIcon.VILLA)

/** The sleeps a new type starts with before staff change it. */
private const val NEW_TYPE_SLEEPS = 2

/**
 * Everything the user has entered or toggled. Fields left null follow what the chosen type
 * suggests, so switching type updates them until the user types over them.
 */
internal data class AddHouseForm(
    /** The existing type chosen; null picks the first one. Ignored while [isNewType]. */
    val typeId: String? = null,
    val isNewType: Boolean = false,
    val newTypeName: String = "",
    val newTypeIcon: HouseIcon = NewTypeIcons.first(),
    /** Null follows the suggested name. */
    val name: String? = null,
    /** Null follows the type's usual sleeps. */
    val sleeps: Int? = null,
    /** Null means today. */
    val bookableFrom: LocalDate? = null,
    val note: String = "",
    val isDatePickerOpen: Boolean = false,
    val showProblems: Boolean = false,
    val isSaving: Boolean = false,
) {
    fun type(catalog: HouseCatalog): HouseType? =
        if (isNewType) null else catalog.types.firstOrNull { it.id == typeId } ?: catalog.types.firstOrNull()

    fun suggestedName(catalog: HouseCatalog): String? = when {
        isNewType -> newTypeName.takeIf(String::isNotBlank)?.let(::firstHouseName)
        else -> type(catalog)?.let { suggestHouseName(it, catalog.houses) }
    }

    fun toDraft(catalog: HouseCatalog): HouseDraft {
        val type = type(catalog)
        return HouseDraft(
            type = if (type == null) TypeChoice.New(newTypeName, newTypeIcon) else TypeChoice.Existing(type),
            name = name ?: suggestedName(catalog).orEmpty(),
            sleeps = sleeps ?: type?.sleeps ?: NEW_TYPE_SLEEPS,
            bookableFrom = bookableFrom ?: catalog.today,
            note = note,
        )
    }
}

internal fun AddHouseForm.toUi(catalog: HouseCatalog): AddHouseFormUi {
    val draft = toDraft(catalog)
    val chosenType = type(catalog)
    return AddHouseFormUi(
        types = catalog.types.map { type ->
            TypeCardUi(
                id = type.id,
                name = type.name,
                icon = type.icon,
                sleeps = type.sleeps,
                houseCount = catalog.houses.count { it.type.id == type.id },
                isSelected = !isNewType && type.id == chosenType?.id,
            )
        },
        isNewType = isNewType,
        newTypeName = newTypeName,
        newTypeIcon = newTypeIcon,
        name = draft.name,
        nameHint = when {
            isNewType -> NameHint.NumberedFromType
            name == null && chosenType != null && draft.name.isNotEmpty() -> NameHint.SuggestedFrom(chosenType.pluralName)
            else -> NameHint.None
        },
        sleeps = draft.sleeps,
        bookableFrom = draft.bookableFrom,
        note = note,
        icon = chosenType?.icon ?: newTypeIcon,
        groupName = draft.groupName,
        houseCount = catalog.houses.size,
        today = catalog.today,
        problems = draft.problems(catalog.houses, catalog.types),
        showProblems = showProblems,
        isDatePickerOpen = isDatePickerOpen,
        isSaving = isSaving,
    )
}
