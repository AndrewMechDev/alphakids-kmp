package org.alphakids.app.store.data

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.dto.AccessoryCatalogDto
import org.alphakids.app.data.remote.dto.BuyAccessoryRequestDto
import org.alphakids.app.data.remote.dto.BuyAccessoryResponseDto
import org.alphakids.app.data.remote.dto.BuyPetRequestDto
import org.alphakids.app.data.remote.dto.InventoryItemDto
import org.alphakids.app.data.remote.dto.PetCatalogDto
import org.alphakids.app.data.remote.dto.StudentPetDto
import org.alphakids.app.store.domain.model.PurchaseResult
import org.alphakids.app.store.domain.repository.StoreRepository

/**
 * Real implementation of [StoreRepository] via [AlphaKidsApiClient].
 */
class StoreRepositoryImpl(
    private val api: AlphaKidsApiClient,
) : StoreRepository {

    override suspend fun getPetsCatalog(studentId: String): List<PetCatalogDto>? =
        getOrNull(ApiConstants.storePetsCatalog(studentId))

    override suspend fun getAccessoriesCatalog(studentId: String): List<AccessoryCatalogDto>? =
        getOrNull(ApiConstants.storeAccessoriesCatalog(studentId))

    override suspend fun getInventory(studentId: String): List<InventoryItemDto>? =
        getOrNull(ApiConstants.studentInventory(studentId))

    override suspend fun buyPet(studentId: String, petCatalogId: String): PurchaseResult<StudentPetDto> =
        purchase(ApiConstants.storeBuyPet(studentId), BuyPetRequestDto(petCatalogId))

    override suspend fun buyAccessory(
        studentId: String,
        accessoryCatalogId: String,
        quantity: Int,
    ): PurchaseResult<BuyAccessoryResponseDto> =
        purchase(ApiConstants.storeBuyAccessory(studentId), BuyAccessoryRequestDto(accessoryCatalogId, quantity))

    private suspend inline fun <reified T> getOrNull(path: String): T? {
        return try {
            val response = api.httpClient.get(path)
            if (!response.status.isSuccess()) return null
            response.body<T>()
        } catch (_: Exception) {
            null
        }
    }

    private suspend inline fun <reified Req : Any, reified Res> purchase(
        path: String,
        request: Req,
    ): PurchaseResult<Res> {
        return try {
            val response = api.httpClient.post(path) { setBody(request) }
            when {
                response.status.isSuccess() -> PurchaseResult.Success(response.body<Res>())
                response.status == HttpStatusCode.BadRequest -> rejection(response.bodyAsText())
                else -> PurchaseResult.Failed
            }
        } catch (_: Exception) {
            PurchaseResult.Failed
        }
    }

    /** Maps the BadRequestException messages thrown by alphakids-api `store.service.ts`. */
    private fun rejection(body: String): PurchaseResult<Nothing> = when {
        NOT_ENOUGH_COINS in body -> PurchaseResult.NotEnoughCoins
        LEVEL_TOO_LOW in body -> PurchaseResult.LevelTooLow
        else -> PurchaseResult.Failed
    }

    private companion object {
        const val NOT_ENOUGH_COINS = "Not enough coins"
        const val LEVEL_TOO_LOW = "Level too low"
    }
}
