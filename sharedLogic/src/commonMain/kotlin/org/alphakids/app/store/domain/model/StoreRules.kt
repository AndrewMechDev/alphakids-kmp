package org.alphakids.app.store.domain.model

import org.alphakids.app.data.remote.dto.AccessoryCatalogDto
import org.alphakids.app.data.remote.dto.PetCatalogDto

/** Whether the child can buy a catalog pet. */
enum class PetAvailability { Owned, Locked, Available }

/**
 * Mirrors alphakids-api `StoreService.buyPet`, which rejects the purchase when
 * `currentLevel < minLevelRequired`. A pet the child already owns is shown
 * as owned regardless of level.
 */
fun PetCatalogDto.availability(childLevel: Int, ownedCatalogIds: Set<String>): PetAvailability = when {
    id in ownedCatalogIds -> PetAvailability.Owned
    childLevel < minLevelRequired -> PetAvailability.Locked
    else -> PetAvailability.Available
}

/** Food restores hunger; every other accessory is a toy that only adds happiness. */
val AccessoryCatalogDto.isFood: Boolean get() = hungerRestore > 0

/** Same comparison the backend makes before charging (`coinsBalance < price` is rejected). */
fun canAfford(balance: Int, price: Int): Boolean = balance >= price
