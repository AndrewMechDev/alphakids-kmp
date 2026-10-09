package org.alphakids.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Prisma `StudentPet` model.
 *
 * - GET /students/:studentId/pets and POST /students/:studentId/store/pets/buy
 *   include [petCatalog].
 * - POST /students/:studentId/pets/:petId/feed returns the bare updated row,
 *   so [petCatalog] is null there — merge the stats into the pet you already
 *   hold instead of replacing it.
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

/** Prisma `PetCatalog` model (GET /students/:studentId/store/catalogs/pets). */
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
 * Body for POST /students/:studentId/pets/:petId/feed.
 *
 * [accessoryId] is an `AccessoryCatalog` id (the backend looks up the
 * student's inventory row by `accessoryCatalogId`), NOT an inventory row id.
 */
@Serializable
data class FeedPetRequestDto(
    val accessoryId: String,
)
