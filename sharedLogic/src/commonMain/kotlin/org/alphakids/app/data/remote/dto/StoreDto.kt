package org.alphakids.app.data.remote.dto

import kotlinx.serialization.Serializable

// ── Requests ──

/** Body for POST /students/:studentId/store/pets/buy. */
@Serializable
data class BuyPetRequestDto(
    val petCatalogId: String,
)

/**
 * Body for POST /students/:studentId/store/accessories/buy.
 *
 * [quantity] has no default on purpose: the API client is configured with
 * `encodeDefaults = false`, so a defaulted field would silently disappear
 * from the request body.
 */
@Serializable
data class BuyAccessoryRequestDto(
    val accessoryCatalogId: String,
    val quantity: Int,
)

// ── Responses ──
//
// POST /students/:studentId/store/pets/buy returns the created StudentPet
// with `include: { petCatalog: true }` — decoded as [StudentPetDto].
// Neither purchase response carries the new coin balance; re-read the
// student's progress after a successful purchase.

/**
 * Response from POST /students/:studentId/store/accessories/buy:
 * the upserted `StudentInventory` row with `include: { accessoryCatalog: true }`.
 */
@Serializable
data class BuyAccessoryResponseDto(
    val id: String,
    val studentId: String,
    val accessoryCatalogId: String,
    val accessoryCatalog: AccessoryCatalogDto? = null,
    val quantity: Int = 0,
)
