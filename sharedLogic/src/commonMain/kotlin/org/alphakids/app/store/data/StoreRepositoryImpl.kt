package org.alphakids.app.store.data

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.apiCall
import org.alphakids.app.data.remote.backendMessage
import org.alphakids.app.data.remote.bodyOrThrow
import org.alphakids.app.data.remote.dto.AccessoryCatalogDto
import org.alphakids.app.data.remote.dto.BuyAccessoryRequestDto
import org.alphakids.app.data.remote.dto.BuyAccessoryResponseDto
import org.alphakids.app.data.remote.dto.BuyPetRequestDto
import org.alphakids.app.data.remote.dto.InventoryItemDto
import org.alphakids.app.data.remote.dto.PetCatalogDto
import org.alphakids.app.data.remote.dto.StudentPetDto
import org.alphakids.app.domain.error.DataError
import org.alphakids.app.domain.error.DataException
import org.alphakids.app.domain.error.asDataError
import org.alphakids.app.store.domain.model.PurchaseResult
import org.alphakids.app.store.domain.repository.StoreRepository

/**
 * Real implementation of [StoreRepository] via [AlphaKidsApiClient].
 */
class StoreRepositoryImpl(
    private val api: AlphaKidsApiClient,
) : StoreRepository {

    override suspend fun getPetsCatalog(studentId: String): Result<List<PetCatalogDto>> =
        get(ApiConstants.storePetsCatalog(studentId))

    override suspend fun getAccessoriesCatalog(studentId: String): Result<List<AccessoryCatalogDto>> =
        get(ApiConstants.storeAccessoriesCatalog(studentId))

    override suspend fun getInventory(studentId: String): Result<List<InventoryItemDto>> =
        get(ApiConstants.studentInventory(studentId))

    override suspend fun buyPet(studentId: String, petCatalogId: String): PurchaseResult<StudentPetDto> =
        purchase(ApiConstants.storeBuyPet(studentId), BuyPetRequestDto(petCatalogId))

    override suspend fun buyAccessory(
        studentId: String,
        accessoryCatalogId: String,
        quantity: Int,
    ): PurchaseResult<BuyAccessoryResponseDto> =
        purchase(ApiConstants.storeBuyAccessory(studentId), BuyAccessoryRequestDto(accessoryCatalogId, quantity))

    private suspend inline fun <reified T> get(path: String): Result<T> =
        apiCall { api.httpClient.get(path).bodyOrThrow() }

    /**
     * Posts a purchase. The two business rejections of `store.service.ts`
     * become typed outcomes; any other failure is [PurchaseResult.Failed]
     * with its [DataError]. Cancellation propagates (see [apiCall]).
     */
    private suspend inline fun <reified Req : Any, reified Res> purchase(
        path: String,
        request: Req,
    ): PurchaseResult<Res> = apiCall<PurchaseResult<Res>> {
        val response = api.httpClient.post(path) { setBody(request) }
        if (response.status == HttpStatusCode.BadRequest) {
            val message = response.backendMessage()
            rejection(message) ?: throw DataException(DataError.Validation(message))
        } else {
            PurchaseResult.Success(response.bodyOrThrow<Res>())
        }
    }.getOrElse { PurchaseResult.Failed(it.asDataError) }

    /** Maps the BadRequestException messages thrown by alphakids-api `store.service.ts`. */
    private fun rejection(message: String?): PurchaseResult<Nothing>? = when {
        message == null -> null
        NOT_ENOUGH_COINS in message -> PurchaseResult.NotEnoughCoins
        LEVEL_TOO_LOW in message -> PurchaseResult.LevelTooLow
        else -> null
    }

    private companion object {
        const val NOT_ENOUGH_COINS = "Not enough coins"
        const val LEVEL_TOO_LOW = "Level too low"
    }
}
