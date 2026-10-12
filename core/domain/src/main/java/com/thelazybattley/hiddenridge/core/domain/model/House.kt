package com.thelazybattley.hiddenridge.core.domain.model

/** The kinds of house the resort rents, in the order staff list them. */
enum class HouseType(val sleeps: Int) {
    CABIN(sleeps = 2),
    CASITA(sleeps = 4),
    FAMILY_HOUSE(sleeps = 8),
}

data class House(
    val id: String,
    val name: String,
    val type: HouseType,
) {
    val sleeps: Int get() = type.sleeps
}
