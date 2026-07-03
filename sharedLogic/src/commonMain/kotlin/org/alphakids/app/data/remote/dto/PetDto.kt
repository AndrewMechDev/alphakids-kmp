package org.alphakids.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Response from GET /pets/:studentId
 * Lists all pets owned by a student.
 */
@Serializable
data class StudentPetDto(
    val id: String,
    val studentId: String,
    val petCatalogId: String,
    val petCatalog: PetCatalogDto? = null,
    val customName: String? = null,
    val hungerLevel: Int = 100,
    val happinessLevel: Int = 100,
    val isActive: Boolean = true,
    val lastFedAt: String? = null,
)

@Serializable
data class PetCatalogDto(
    val id: String,
    val name: String = "",
    val species: String = "",
    val imageUrl: String? = null,
    val coinPrice: Int = 0,
    val minLevelRequired: Int = 0,
    val isActive: Boolean = true,
)

/**
 * Request/response for POST /pets/:petId/feed
 */
@Serializable
data class FeedPetRequestDto(
    val inventoryItemId: String,
)

@Serializable
data class FeedPetResponseDto(
    val success: Boolean,
    val pet: StudentPetDto? = null,
    val newHunger: Int = 0,
    val newHappiness: Int = 0,
)
