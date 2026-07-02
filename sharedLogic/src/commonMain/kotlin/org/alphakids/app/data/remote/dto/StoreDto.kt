package org.alphakids.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Requests ──

@Serializable
data class BuyPetRequestDto(
    @SerialName("pet_catalog_id")
    val petCatalogId: String,
)

@Serializable
data class BuyAccessoryRequestDto(
    @SerialName("accessory_catalog_id")
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
    @SerialName("student_id")
    val studentId: String,
    @SerialName("pet_catalog_id")
    val petCatalogId: String,
    @SerialName("pet_catalog")
    val petCatalog: PetCatalogDto? = null,
    @SerialName("custom_name")
    val customName: String? = null,
    @SerialName("hunger_level")
    val hungerLevel: Int = 100,
    @SerialName("happiness_level")
    val happinessLevel: Int = 100,
    @SerialName("is_active")
    val isActive: Boolean = true,
)

/**
 * Response from POST /students/:studentId/store/accessories/buy
 * Returns the updated inventory item.
 */
@Serializable
data class BuyAccessoryResponseDto(
    val id: String,
    @SerialName("student_id")
    val studentId: String,
    @SerialName("accessory_catalog_id")
    val accessoryCatalogId: String,
    @SerialName("accessory_catalog")
    val accessoryCatalog: AccessoryCatalogDto? = null,
    val quantity: Int = 0,
)
