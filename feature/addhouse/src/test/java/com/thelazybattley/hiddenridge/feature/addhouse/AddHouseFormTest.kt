package com.thelazybattley.hiddenridge.feature.addhouse

import com.thelazybattley.hiddenridge.core.domain.model.House
import com.thelazybattley.hiddenridge.core.domain.model.HouseIcon
import com.thelazybattley.hiddenridge.core.domain.model.HouseProblem
import com.thelazybattley.hiddenridge.core.domain.model.HouseType
import com.thelazybattley.hiddenridge.core.domain.model.TypeChoice
import com.thelazybattley.hiddenridge.core.domain.usecase.HouseCatalog
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddHouseFormTest {

    private val today = LocalDate.of(2026, 10, 12)
    private val catalog = HouseCatalog(
        today = today,
        houses = listOf(
            House("c1", "Cabin 1", HouseType.CABIN),
            House("c2", "Cabin 2", HouseType.CABIN),
            House("c3", "Cabin 3", HouseType.CABIN),
            House("ca", "Casita A", HouseType.CASITA),
            House("f1", "Ridge House", HouseType.FAMILY_HOUSE),
        ),
        types = HouseType.BUILT_IN,
    )

    @Test
    fun `the first type is picked and the next house name suggested`() {
        val ui = AddHouseForm().toUi(catalog)

        assertEquals(listOf(true, false, false), ui.types.map(TypeCardUi::isSelected))
        assertEquals(listOf(3, 1, 1), ui.types.map(TypeCardUi::houseCount))
        assertEquals("Cabin 4", ui.name)
        assertEquals(NameHint.SuggestedFrom("Cabins"), ui.nameHint)
        assertEquals(2, ui.sleeps)
        assertEquals(today, ui.bookableFrom)
        assertEquals("Cabins", ui.groupName)
        assertEquals(HouseIcon.CABIN, ui.icon)
    }

    @Test
    fun `houses with their own names get no suggestion`() {
        val ui = AddHouseForm(typeId = HouseType.FAMILY_HOUSE.id).toUi(catalog)

        assertEquals("", ui.name)
        assertEquals(NameHint.None, ui.nameHint)
        assertEquals(8, ui.sleeps)
    }

    @Test
    fun `a typed name and sleeps stick`() {
        val ui = AddHouseForm(name = "Hilltop", sleeps = 5).toUi(catalog)

        assertEquals("Hilltop", ui.name)
        assertEquals(NameHint.None, ui.nameHint)
        assertEquals(5, ui.sleeps)
    }

    @Test
    fun `a new type is named by staff and numbers its first house`() {
        val form = AddHouseForm(isNewType = true, newTypeName = "Treehouse", newTypeIcon = HouseIcon.TREE)
        val ui = form.toUi(catalog)

        assertTrue(ui.types.none(TypeCardUi::isSelected))
        assertEquals("Treehouse 1", ui.name)
        assertEquals(NameHint.NumberedFromType, ui.nameHint)
        assertEquals("Treehouses", ui.groupName)
        assertEquals(HouseIcon.TREE, ui.icon)
        assertEquals(TypeChoice.New("Treehouse", HouseIcon.TREE), form.toDraft(catalog).type)
    }

    @Test
    fun `problems come from the draft`() {
        val ui = AddHouseForm(name = "cabin 1", showProblems = true).toUi(catalog)

        assertTrue(ui.shows(HouseProblem.NAME_TAKEN))
    }
}
