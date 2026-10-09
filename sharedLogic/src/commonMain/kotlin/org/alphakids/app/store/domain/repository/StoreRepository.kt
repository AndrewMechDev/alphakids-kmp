package org.alphakids.app.store.domain.repository

import org.alphakids.app.data.remote.dto.AccessoryCatalogDto
import org.alphakids.app.data.remote.dto.BuyAccessoryResponseDto
import org.alphakids.app.data.remote.dto.InventoryItemDto
import org.alphakids.app.data.remote.dto.PetCatalogDto
import org.alphakids.app.data.remote.dto.StudentPetDto
import org.alphakids.app.store.domain.model.PurchaseResult

/**
 * Repository for the in-app store (economía virtual).
 *
 * Endpoints (alphakids-api `src/store/store.controller.ts`, and the
 * inventory route in `src/students/students.controller.ts`):
 * - GET /students/:studentId/store/catalogs/pets
 * - GET /students/:studentId/store/catalogs/accessories
 * - POST /students/:studentId/store/pets/buy `{ petCatalogId }`
 * - POST /students/:studentId/store/accessories/buy `{ accessoryCatalogId, quantity }`
 * - GET /students/:studentId/inventory
 *
 * Read methods return null when the request failed, so callers can tell a
 * failure apart from a genuinely empty catalog or inventory.
 */
interface StoreRepository {
    suspend fun getPetsCatalog(studentId: String): List<PetCatalogDto>?
    suspend fun getAccessoriesCatalog(studentId: String): List<AccessoryCatalogDto>?
    suspend fun getInventory(studentId: String): List<InventoryItemDto>?
    suspend fun buyPet(studentId: String, petCatalogId: String): PurchaseResult<StudentPetDto>
    suspend fun buyAccessory(
        studentId: String,
        accessoryCatalogId: String,
        quantity: Int = 1,
    ): PurchaseResult<BuyAccessoryResponseDto>
}
