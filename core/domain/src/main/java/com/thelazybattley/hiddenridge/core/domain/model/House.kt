package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate

/** The pictures a house type can be shown with. */
enum class HouseIcon {
    CABIN,
    COTTAGE,
    VILLAGE,
    TREE,
    TENT,
    VILLA,
}

/** A kind of house staff group by, such as cabins. Staff can add their own when adding a house. */
data class HouseType(
    val id: String,
    /** One house of this type: "Cabin". */
    val name: String,
    /** The group heading: "Cabins". */
    val pluralName: String,
    val icon: HouseIcon,
    /** What a new house of this type sleeps unless staff change it. */
    val sleeps: Int,
) {
    companion object {
        val CABIN = HouseType("CABIN", "Cabin", "Cabins", HouseIcon.CABIN, sleeps = 2)
        val CASITA = HouseType("CASITA", "Casita", "Casitas", HouseIcon.COTTAGE, sleeps = 4)
        val FAMILY_HOUSE = HouseType("FAMILY_HOUSE", "Family house", "Family houses", HouseIcon.VILLAGE, sleeps = 8)

        /** The types the resort started with, in the order staff list them. */
        val BUILT_IN: List<HouseType> = listOf(CABIN, CASITA, FAMILY_HOUSE)

        /** "Treehouse" → "Treehouses", for a type staff name themselves. */
        fun pluralOf(name: String): String {
            val trimmed = name.trim()
            val lower = trimmed.lowercase()
            return when {
                lower.endsWith("s") || lower.endsWith("x") || lower.endsWith("ch") || lower.endsWith("sh") -> "${trimmed}es"
                lower.endsWith("y") && lower.dropLast(1).lastOrNull()?.let { it !in "aeiou" } == true -> "${trimmed.dropLast(1)}ies"
                else -> "${trimmed}s"
            }
        }
    }
}

data class House(
    val id: String,
    val name: String,
    val type: HouseType,
    val sleeps: Int = type.sleeps,
    /** The first night guests can stay; earlier nights are closed. Null when it has always been open. */
    val bookableFrom: LocalDate? = null,
    /** What staff should know when picking this house, such as step-free access. */
    val note: String = "",
) {
    /** Whether guests can stay on the night of [night]. */
    fun isOpenOn(night: LocalDate): Boolean = bookableFrom == null || !night.isBefore(bookableFrom)
}

/** Each type that has houses, in the order its first house is listed. */
fun List<House>.types(): List<HouseType> = map(House::type).distinctBy(HouseType::id)
