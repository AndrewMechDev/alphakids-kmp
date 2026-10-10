package org.alphakids.app.store.domain.model

import org.alphakids.app.data.remote.dto.AccessoryCatalogDto
import org.alphakids.app.data.remote.dto.PetCatalogDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoreRulesTest {

    private val owl = PetCatalogDto(id = "cat_3", name = "Búho Sabio", species = "Owl", coinPrice = 500, minLevelRequired = 2)

    @Test
    fun a_pet_above_the_childs_level_is_locked() {
        assertEquals(PetAvailability.Locked, owl.availability(childLevel = 1, ownedCatalogIds = emptySet()))
    }

    @Test
    fun the_level_requirement_is_inclusive_like_the_backend_check() {
        assertEquals(PetAvailability.Available, owl.availability(childLevel = 2, ownedCatalogIds = emptySet()))
    }

    @Test
    fun ownership_wins_over_the_level_lock() {
        assertEquals(PetAvailability.Owned, owl.availability(childLevel = 1, ownedCatalogIds = setOf("cat_3")))
    }

    @Test
    fun only_accessories_that_restore_hunger_are_food() {
        assertTrue(AccessoryCatalogDto(id = "acc_1", hungerRestore = 20, happinessRestore = 5).isFood)
        assertFalse(AccessoryCatalogDto(id = "acc_2", hungerRestore = 0, happinessRestore = 30).isFood)
    }

    @Test
    fun affordability_accepts_an_exact_balance_and_rejects_a_short_one() {
        assertTrue(canAfford(balance = 100, price = 100))
        assertFalse(canAfford(balance = 0, price = 10))
    }
}
