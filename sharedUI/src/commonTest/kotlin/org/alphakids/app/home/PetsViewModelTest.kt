package org.alphakids.app.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.alphakids.app.data.remote.dto.BuyAccessoryResponseDto
import org.alphakids.app.data.remote.dto.InventoryItemDto
import org.alphakids.app.parent.domain.model.GameProgressManager
import org.alphakids.app.store.domain.model.PurchaseResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PetsViewModelTest {

    private val game = FakeGameRepository(progress(coins = 0))
    private val store = FakeStoreRepository()
    private val pets = FakeStudentPetRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        GameProgressManager.clear()
        Dispatchers.resetMain()
    }

    private fun viewModel() = PetsViewModel(STUDENT, store, pets, game)

    @Test
    fun a_zero_balance_from_the_backend_is_shown_as_zero() {
        val state = viewModel().state.value

        assertFalse(state.isLoading)
        assertFalse(state.loadFailed)
        assertEquals(0, state.coins)
    }

    @Test
    fun a_failed_request_shows_the_error_state_instead_of_an_empty_store() {
        store.inventory = null

        assertTrue(viewModel().state.value.loadFailed)
    }

    @Test
    fun a_rejected_purchase_grants_nothing_and_explains_why() {
        game.progress = progress(coins = 5)
        store.buyAccessoryResult = PurchaseResult.NotEnoughCoins
        val vm = viewModel()

        vm.buyAccessory(APPLE)

        val state = vm.state.value
        assertTrue(state.inventory.isEmpty())
        assertEquals(5, state.coins)
        assertEquals(PetsNotice.NotEnoughCoins, state.notice)
    }

    @Test
    fun a_failed_purchase_grants_nothing() {
        store.buyPetResult = PurchaseResult.Failed
        val vm = viewModel()

        vm.buyPet(CAT)

        assertTrue(vm.state.value.pets.isEmpty())
        assertEquals(PetsNotice.PurchaseFailed, vm.state.value.notice)
    }

    @Test
    fun a_successful_purchase_shows_the_server_inventory_and_the_refreshed_server_balance() {
        game.progress = progress(coins = 100)
        val vm = viewModel()
        store.buyAccessoryResult = PurchaseResult.Success(
            BuyAccessoryResponseDto(id = "inv_1", studentId = STUDENT, accessoryCatalogId = APPLE.id, quantity = 1),
        )
        game.progress = progress(coins = 90) // balance after the backend charged the purchase

        vm.buyAccessory(APPLE)

        val state = vm.state.value
        assertEquals(90, state.coins)
        assertEquals(listOf(1), state.inventory.map { it.quantity })
        assertEquals(APPLE, state.inventory.single().accessory)
        assertIs<PetsNotice.Purchased>(state.notice)
    }

    @Test
    fun feeding_sends_the_accessory_catalog_id_and_keeps_the_pet_catalog() {
        pets.pets = listOf(ownedPet(hunger = 40))
        pets.fedPet = ownedPet(hunger = 60).copy(petCatalog = null)
        store.inventory = listOf(InventoryItemDto(id = "inv_1", quantity = 2, accessory = APPLE))
        val vm = viewModel()

        vm.feed(vm.state.value.pets.single(), vm.state.value.inventory.single())

        val state = vm.state.value
        assertEquals(APPLE.id, pets.lastFedAccessoryId)
        assertEquals(60, state.pets.single().hungerLevel)
        assertEquals(CAT, state.pets.single().petCatalog)
        assertEquals(1, state.inventory.single().quantity)
    }

    @Test
    fun playing_changes_no_stat_because_there_is_no_backend_endpoint() {
        pets.pets = listOf(ownedPet(hunger = 40))
        val vm = viewModel()

        vm.play(vm.state.value.pets.single())

        assertEquals(listOf(ownedPet(hunger = 40)), vm.state.value.pets)
        assertIs<PetsNotice.Played>(vm.state.value.notice)
    }
}
