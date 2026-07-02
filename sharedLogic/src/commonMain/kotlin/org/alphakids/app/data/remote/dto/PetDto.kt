package org.alphakids.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from GET /pets/:studentId
 * Lists all pets owned by a student.
 */
@Serializable
data class StudentPetDto(
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
    @SerialName("last_fed_at")
    val lastFedAt: String? = null,
)

@Serializable
data class PetCatalogDto(
    val id: String,
    val name: String = "",
    val species: String = "",
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("coin_price")
    val coinPrice: Int = 0,
    @SerialName("min_level_required")
    val minLevelRequired: Int = 0,
    @SerialName("is_active")
    val isActive: Boolean = true,
)

/**
 * Request/response for POST /pets/:petId/feed
 */
@Serializable
data class FeedPetRequestDto(
    @SerialName("inventory_item_id")
    val inventoryItemId: String,
)

@Serializable
data class FeedPetResponseDto(
    val success: Boolean,
    val pet: StudentPetDto? = null,
    @SerialName("new_hunger")
    val newHunger: Int = 0,
    @SerialName("new_happiness")
    val newHappiness: Int = 0,
)
