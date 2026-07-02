package org.alphakids.app.store.domain.repository

import org.alphakids.app.data.remote.dto.AccessoryCatalogDto
import org.alphakids.app.data.remote.dto.BuyAccessoryRequestDto
import org.alphakids.app.data.remote.dto.BuyAccessoryResponseDto
import org.alphakids.app.data.remote.dto.BuyPetRequestDto
import org.alphakids.app.data.remote.dto.BuyPetResponseDto
import org.alphakids.app.data.remote.dto.PetCatalogDto

/**
 * Repository for the in-app store (economía virtual).
 *
 * Endpoints:
 * - GET /students/:studentId/store/catalogs/pets
 * - GET /students/:studentId/store/catalogs/accessories
 * - POST /students/:studentId/store/pets/buy
 * - POST /students/:studentId/store/accessories/buy
 */
interface StoreRepository {
    suspend fun getPetsCatalog(studentId: String): List<PetCatalogDto>
    suspend fun getAccessoriesCatalog(studentId: String): List<AccessoryCatalogDto>
    suspend fun buyPet(studentId: String, request: BuyPetRequestDto): BuyPetResponseDto?
    suspend fun buyAccessory(studentId: String, request: BuyAccessoryRequestDto): BuyAccessoryResponseDto?
}
