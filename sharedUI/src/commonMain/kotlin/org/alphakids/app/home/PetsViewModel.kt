package org.alphakids.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.alphakids.app.data.remote.dto.AccessoryCatalogDto
import org.alphakids.app.data.remote.dto.InventoryItemDto
import org.alphakids.app.data.remote.dto.PetCatalogDto
import org.alphakids.app.data.remote.dto.StudentPetDto
import org.alphakids.app.data.remote.dto.StudentProgressDto
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.parent.domain.model.GameProgressManager
import org.alphakids.app.store.domain.model.PurchaseResult
import org.alphakids.app.store.domain.repository.StoreRepository
import org.alphakids.app.studentpet.domain.repository.StudentPetRepository

/**
 * A short, child-friendly message shown after an action on the Mascotas tab.
 *
 * @param isError Errors are shown in the error color, confirmations in green.
 */
sealed class PetsNotice(val text: String, val isError: Boolean) {
    class Purchased(name: String) : PetsNotice("¡Ya tienes $name!", isError = false)
    class Fed(petName: String) : PetsNotice("¡$petName comió muy rico!", isError = false)
    class Played(petName: String) : PetsNotice("¡$petName se divirtió mucho contigo!", isError = false)
    data object NotEnoughCoins : PetsNotice("Te faltan monedas. ¡Juega para ganar más!", isError = true)
    data object LevelTooLow : PetsNotice("Aún no tienes el nivel para esta mascota.", isError = true)
    data object PurchaseFailed : PetsNotice("No pudimos completar la compra. Inténtalo otra vez.", isError = true)
    data object FeedFailed : PetsNotice("No pudimos darle de comer. Inténtalo otra vez.", isError = true)
    data object BalanceOutdated : PetsNotice("Compra lista, pero no pudimos actualizar tus monedas.", isError = true)
}

/**
 * UI state for the Mascotas tab (Mis Mascotas + Tienda).
 *
 * Everything here comes from the backend; nothing is granted or computed
 * locally except the derived views ([ownedCatalogIds]).
 *
 * @param isLoading True while the initial load runs.
 * @param loadFailed True when any required request failed; the screen shows
 *   a retry state instead of a partial or stale store.
 * @param coins Server balance of the active child, as last read by this screen.
 * @param childLevel Server level of the active child, drives pet locks.
 * @param isBusy True while a purchase or feed request is in flight.
 */
data class PetsUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val coins: Int = 0,
    val childLevel: Int = 1,
    val pets: List<StudentPetDto> = emptyList(),
    val petCatalog: List<PetCatalogDto> = emptyList(),
    val accessories: List<AccessoryCatalogDto> = emptyList(),
    val inventory: List<InventoryItemDto> = emptyList(),
    val isBusy: Boolean = false,
    val notice: PetsNotice? = null,
) {
    val ownedCatalogIds: Set<String> get() = pets.mapTo(mutableSetOf()) { it.petCatalogId }

    /** Inventory rows the child can still use (quantity left and a known accessory). */
    val usableInventory: List<InventoryItemDto>
        get() = inventory.filter { it.quantity > 0 && it.accessory != null }
}

/**
 * ViewModel for the Mascotas tab, wired to the real store and pets API.
 *
 * Purchases and feeding go through the backend; the UI only reflects what
 * the server confirmed. Store responses carry no coin balance, so after a
 * purchase the balance is re-read from GET /students/:id/achievements.
 */
class PetsViewModel(
    private val studentId: String,
    private val storeRepository: StoreRepository,
    private val studentPetRepository: StudentPetRepository,
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PetsUiState())
    val state: StateFlow<PetsUiState> = _state.asStateFlow()

    init {
        load()
    }

    /** Loads pets, catalogs, inventory and progress in parallel. Safe to call again (retry). */
    fun load() {
        _state.update { it.copy(isLoading = true, loadFailed = false) }
        viewModelScope.launch {
            val pets = async { studentPetRepository.getPets(studentId) }
            val petCatalog = async { storeRepository.getPetsCatalog(studentId) }
            val accessories = async { storeRepository.getAccessoriesCatalog(studentId) }
            val inventory = async { storeRepository.getInventory(studentId) }
            val progress = async { gameRepository.getAchievements(studentId)?.progress }

            val serverProgress = progress.await()?.also(::applyProgress)
            val loaded = listOf(pets.await(), petCatalog.await(), accessories.await(), inventory.await())
            if (loaded.any { it == null } || serverProgress == null) {
                _state.update { it.copy(isLoading = false, loadFailed = true) }
                return@launch
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    pets = pets.await().orEmpty(),
                    petCatalog = petCatalog.await().orEmpty().filter { pet -> pet.isActive },
                    accessories = accessories.await().orEmpty().filter { acc -> acc.isActive },
                    inventory = inventory.await().orEmpty(),
                )
            }
        }
    }

    fun buyPet(catalogPet: PetCatalogDto) = runExclusive {
        val result = storeRepository.buyPet(studentId, catalogPet.id)
        if (result is PurchaseResult.Success) {
            _state.update { it.copy(pets = it.pets + result.value) }
        }
        finishPurchase(result, catalogPet.name)
    }

    fun buyAccessory(accessory: AccessoryCatalogDto) = runExclusive {
        val result = storeRepository.buyAccessory(studentId, accessory.id)
        if (result is PurchaseResult.Success) {
            val row = result.value
            val item = InventoryItemDto(row.id, row.quantity, row.accessoryCatalog ?: accessory)
            _state.update { state ->
                state.copy(inventory = state.inventory.filterNot { it.id == row.id } + item)
            }
        }
        finishPurchase(result, accessory.name)
    }

    /**
     * Feeds [pet] with one unit of [item]. The backend answers with the bare
     * pet row (no catalog), so only its stats are merged into the pet we hold;
     * a 2xx also means exactly one unit was taken from the inventory
     * (alphakids-api `pets.service.ts`).
     */
    fun feed(pet: StudentPetDto, item: InventoryItemDto) = runExclusive {
        val accessory = item.accessory ?: return@runExclusive
        val fed = studentPetRepository.feedPet(studentId, pet.id, accessory.id)
        if (fed == null) {
            _state.update { it.copy(notice = PetsNotice.FeedFailed) }
            return@runExclusive
        }
        _state.update { state ->
            state.copy(
                pets = state.pets.map {
                    if (it.id == fed.id) {
                        it.copy(hungerLevel = fed.hungerLevel, happinessLevel = fed.happinessLevel, lastFedAt = fed.lastFedAt)
                    } else {
                        it
                    }
                },
                inventory = state.inventory.map { if (it.id == item.id) it.copy(quantity = it.quantity - 1) else it },
                notice = PetsNotice.Fed(pet.displayName),
            )
        }
    }

    /**
     * Playing is a purely visual interaction: the backend has no endpoint
     * for it, so no stat is changed or pretended to be saved.
     */
    fun play(pet: StudentPetDto) {
        _state.update { it.copy(notice = PetsNotice.Played(pet.displayName)) }
    }

    fun dismissNotice() {
        _state.update { it.copy(notice = null) }
    }

    private suspend fun finishPurchase(result: PurchaseResult<*>, itemName: String) {
        // Any answer from the store may mean our balance was stale; re-read it.
        val refreshed = refreshEconomy()
        val notice = when (result) {
            is PurchaseResult.Success -> if (refreshed) PetsNotice.Purchased(itemName) else PetsNotice.BalanceOutdated
            PurchaseResult.NotEnoughCoins -> PetsNotice.NotEnoughCoins
            PurchaseResult.LevelTooLow -> PetsNotice.LevelTooLow
            PurchaseResult.Failed -> PetsNotice.PurchaseFailed
        }
        _state.update { it.copy(notice = notice) }
    }

    private suspend fun refreshEconomy(): Boolean {
        val progress = gameRepository.getAchievements(studentId)?.progress ?: return false
        applyProgress(progress)
        return true
    }

    /** Shows the server progress here and shares it with the other tabs. */
    private fun applyProgress(progress: StudentProgressDto) {
        GameProgressManager.applyServerProgress(progress)
        _state.update { it.copy(coins = progress.coinsBalance, childLevel = progress.currentLevel) }
    }

    /** Runs one store/feed action at a time so a double tap can't buy twice. */
    private fun runExclusive(action: suspend () -> Unit) {
        if (_state.value.isBusy) return
        _state.update { it.copy(isBusy = true, notice = null) }
        viewModelScope.launch {
            try {
                action()
            } finally {
                _state.update { it.copy(isBusy = false) }
            }
        }
    }
}

/** The child's chosen name, falling back to the catalog name. */
val StudentPetDto.displayName: String
    get() = customName?.takeIf { it.isNotBlank() } ?: petCatalog?.name ?: "Tu mascota"
