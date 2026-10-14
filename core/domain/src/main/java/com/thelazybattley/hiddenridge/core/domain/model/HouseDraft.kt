package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate

/** Which type a house being added belongs to. */
sealed interface TypeChoice {
    data class Existing(val type: HouseType) : TypeChoice

    /** A type staff are naming now; it becomes a new group on the timeline and Houses tab. */
    data class New(val name: String = "", val icon: HouseIcon = HouseIcon.TREE) : TypeChoice
}

/** A house being filled in before it's added. See [problems]. */
data class HouseDraft(
    val type: TypeChoice,
    val name: String,
    val sleeps: Int,
    val bookableFrom: LocalDate,
    val note: String = "",
) {
    val isNewType: Boolean get() = type is TypeChoice.New

    /** The group heading this house will appear under, once it has a type name. */
    val groupName: String
        get() = when (type) {
            is TypeChoice.Existing -> type.type.pluralName
            is TypeChoice.New -> type.name.takeIf(String::isNotBlank)?.let(HouseType::pluralOf).orEmpty()
        }

    /** What still stops this house from being added, in the order the form asks for it. */
    fun problems(houses: List<House>, types: List<HouseType>): List<HouseProblem> = buildList {
        if (type is TypeChoice.New) {
            val typeName = type.name.trim()
            when {
                typeName.isEmpty() -> add(HouseProblem.MISSING_TYPE_NAME)
                types.any { it.name.equals(typeName, ignoreCase = true) || it.pluralName.equals(typeName, ignoreCase = true) } ->
                    add(HouseProblem.TYPE_NAME_TAKEN)
            }
        }
        val trimmed = name.trim()
        when {
            trimmed.isEmpty() -> add(HouseProblem.MISSING_NAME)
            houses.any { it.name.equals(trimmed, ignoreCase = true) } -> add(HouseProblem.NAME_TAKEN)
        }
        if (sleeps !in SLEEPS_RANGE) add(HouseProblem.SLEEPS_OUT_OF_RANGE)
    }

    companion object {
        val SLEEPS_RANGE = 1..20
    }
}

enum class HouseProblem {
    MISSING_TYPE_NAME,
    TYPE_NAME_TAKEN,
    MISSING_NAME,
    NAME_TAKEN,
    SLEEPS_OUT_OF_RANGE,
}

private val NUMBERED = Regex("""^(.*\S)\s+(\d+)$""")
private val LETTERED = Regex("""^(.*\S)\s+([A-Z])$""")

/**
 * The next name in the pattern the other houses of [type] follow: "Cabin 4" after Cabins 1–3,
 * "Casita D" after Casitas A–C. Null when they don't share a pattern, such as named houses.
 */
fun suggestHouseName(type: HouseType, houses: List<House>): String? {
    val names = houses.filter { it.type.id == type.id }.map { it.name.trim() }
    if (names.isEmpty()) return firstHouseName(type.name)
    sharedPrefix(names, NUMBERED)?.let { (prefix, matches) ->
        return "$prefix ${matches.maxOf { it.groupValues[2].toInt() } + 1}"
    }
    sharedPrefix(names, LETTERED)?.let { (prefix, matches) ->
        val last = matches.maxOf { it.groupValues[2].single() }
        if (last < 'Z') return "$prefix ${last + 1}"
    }
    return null
}

/** The prefix every name in [names] shares before its [pattern] suffix, or null if any differs. */
private fun sharedPrefix(names: List<String>, pattern: Regex): Pair<String, List<MatchResult>>? {
    val matches = names.mapNotNull(pattern::matchEntire)
    val prefix = matches.map { it.groupValues[1] }.distinct().singleOrNull()
    return if (matches.size == names.size && prefix != null) prefix to matches else null
}

/** A new type's houses are numbered from its name: "Treehouse 1". */
fun firstHouseName(typeName: String): String = "${typeName.trim()} 1"
