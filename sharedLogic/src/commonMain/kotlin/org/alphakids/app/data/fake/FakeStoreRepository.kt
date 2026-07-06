package org.alphakids.app.data.fake

import org.alphakids.app.data.remote.dto.AccessoryCatalogDto
import org.alphakids.app.data.remote.dto.BuyAccessoryRequestDto
import org.alphakids.app.data.remote.dto.BuyAccessoryResponseDto
import org.alphakids.app.data.remote.dto.BuyPetRequestDto
import org.alphakids.app.data.remote.dto.BuyPetResponseDto
import org.alphakids.app.data.remote.dto.PetCatalogDto
import org.alphakids.app.store.domain.repository.StoreRepository

class FakeStoreRepository : StoreRepository {

    override suspend fun getPetsCatalog(studentId: String): List<PetCatalogDto> = listOf(
        PetCatalogDto(id = "cat-1", name = "Draco", species = "DRAGON", coinPrice = 0, minLevelRequired = 0),
        PetCatalogDto(id = "cat-2", name = "Unicornio", species = "UNICORN", coinPrice = 100, minLevelRequired = 2),
        PetCatalogDto(id = "cat-3", name = "Fénix", species = "PHOENIX", coinPrice = 200, minLevelRequired = 5),
    )

    override suspend fun getAccessoriesCatalog(studentId: String): List<AccessoryCatalogDto> = listOf(
        AccessoryCatalogDto(id = "acc-1", name = "Galleta Mágica", coinPrice = 10, hungerRestore = 30, happinessRestore = 10),
        AccessoryCatalogDto(id = "acc-2", name = "Poción de Alegría", coinPrice = 15, hungerRestore = 10, happinessRestore = 40),
    )

    override suspend fun buyPet(studentId: String, request: BuyPetRequestDto): BuyPetResponseDto =
        BuyPetResponseDto(id = "new-pet-${request.petCatalogId}", studentId = studentId, petCatalogId = request.petCatalogId)

    override suspend fun buyAccessory(studentId: String, request: BuyAccessoryRequestDto): BuyAccessoryResponseDto =
        BuyAccessoryResponseDto(id = "inv-${request.accessoryCatalogId}", studentId = studentId, accessoryCatalogId = request.accessoryCatalogId, quantity = request.quantity)
}
