package org.alphakids.app.data.remote.dto

import kotlinx.serialization.Serializable

// ── Requests ──

@Serializable
data class BuyPetRequestDto(
    val petCatalogId: String,
)

@Serializable
data class BuyAccessoryRequestDto(
    val accessoryCatalogId: String,
    val quantity: Int = 1,
)

// ── Responses ──

/**
 * Response from POST /students/:studentId/store/pets/buy
 * Returns the created StudentPet with included catalog info.
 */
@Serializable
data class BuyPetResponseDto(
    val id: String,
    val studentId: String,
    val petCatalogId: String,
    val petCatalog: PetCatalogDto? = null,
    val customName: String? = null,
    val hungerLevel: Int = 100,
    val happinessLevel: Int = 100,
    val isActive: Boolean = true,
)

/**
 * Response from POST /students/:studentId/store/accessories/buy
 * Returns the updated inventory item.
 */
@Serializable
data class BuyAccessoryResponseDto(
    val id: String,
    val studentId: String,
    val accessoryCatalogId: String,
    val accessoryCatalog: AccessoryCatalogDto? = null,
    val quantity: Int = 0,
)
