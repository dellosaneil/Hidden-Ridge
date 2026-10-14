package com.thelazybattley.hiddenridge.core.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HouseDraftTest {

    private val today = LocalDate.of(2026, 10, 12)
    private val houses = listOf(
        House("c1", "Cabin 1", HouseType.CABIN),
        House("c2", "Cabin 2", HouseType.CABIN),
        House("c3", "Cabin 3", HouseType.CABIN),
        House("ca", "Casita A", HouseType.CASITA),
        House("cb", "Casita B", HouseType.CASITA),
        House("f1", "Ridge House", HouseType.FAMILY_HOUSE),
        House("f2", "Lake House", HouseType.FAMILY_HOUSE),
    )

    @Test
    fun `names follow the numbers or letters the other houses use`() {
        assertEquals("Cabin 4", suggestHouseName(HouseType.CABIN, houses))
        assertEquals("Casita C", suggestHouseName(HouseType.CASITA, houses))
        assertNull(suggestHouseName(HouseType.FAMILY_HOUSE, houses))
    }

    @Test
    fun `the first house of a type is numbered from its name`() {
        val treehouse = HouseType("t", "Treehouse", "Treehouses", HouseIcon.TREE, sleeps = 2)

        assertEquals("Treehouse 1", suggestHouseName(treehouse, houses))
    }

    @Test
    fun `plurals follow english spelling`() {
        assertEquals("Treehouses", HouseType.pluralOf("Treehouse"))
        assertEquals("Lodges", HouseType.pluralOf(" Lodge "))
        assertEquals("Bunkies", HouseType.pluralOf("Bunky"))
        assertEquals("Bays", HouseType.pluralOf("Bay"))
        assertEquals("Ranches", HouseType.pluralOf("Ranch"))
    }

    @Test
    fun `a house needs a name no other house has`() {
        assertEquals(listOf(HouseProblem.MISSING_NAME), draft(name = " ").problems(houses, HouseType.BUILT_IN))
        assertEquals(listOf(HouseProblem.NAME_TAKEN), draft(name = "cabin 2").problems(houses, HouseType.BUILT_IN))
        assertEquals(emptyList<HouseProblem>(), draft(name = "Cabin 4").problems(houses, HouseType.BUILT_IN))
    }

    @Test
    fun `a new type needs a name no other type has`() {
        assertEquals(
            listOf(HouseProblem.MISSING_TYPE_NAME),
            draft(type = TypeChoice.New(name = ""), name = "Treehouse 1").problems(houses, HouseType.BUILT_IN),
        )
        assertEquals(
            listOf(HouseProblem.TYPE_NAME_TAKEN),
            draft(type = TypeChoice.New(name = "Cabins"), name = "Treehouse 1").problems(houses, HouseType.BUILT_IN),
        )
        assertEquals("Treehouses", draft(type = TypeChoice.New(name = "Treehouse"), name = "Treehouse 1").groupName)
    }

    @Test
    fun `sleeps stays within the house limits`() {
        assertEquals(listOf(HouseProblem.SLEEPS_OUT_OF_RANGE), draft(name = "Cabin 4", sleeps = 0).problems(houses, HouseType.BUILT_IN))
    }

    @Test
    fun `a house is closed before it is bookable`() {
        val house = House("n", "Cabin 4", HouseType.CABIN, bookableFrom = today.plusDays(3))

        assertEquals(false, house.isOpenOn(today.plusDays(2)))
        assertEquals(true, house.isOpenOn(today.plusDays(3)))
        assertEquals(true, House("c", "Cabin 1", HouseType.CABIN).isOpenOn(today))
    }

    private fun draft(
        type: TypeChoice = TypeChoice.Existing(HouseType.CABIN),
        name: String,
        sleeps: Int = 2,
    ) = HouseDraft(type = type, name = name, sleeps = sleeps, bookableFrom = today)
}
