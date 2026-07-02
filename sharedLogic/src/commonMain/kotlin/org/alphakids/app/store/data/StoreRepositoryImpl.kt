package org.alphakids.app.store.data

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.dto.AccessoryCatalogDto
import org.alphakids.app.data.remote.dto.BuyAccessoryRequestDto
import org.alphakids.app.data.remote.dto.BuyAccessoryResponseDto
import org.alphakids.app.data.remote.dto.BuyPetRequestDto
import org.alphakids.app.data.remote.dto.BuyPetResponseDto
import org.alphakids.app.data.remote.dto.PetCatalogDto
import org.alphakids.app.store.domain.repository.StoreRepository

/**
 * Real implementation of [StoreRepository] via [AlphaKidsApiClient].
 */
class StoreRepositoryImpl(
    private val api: AlphaKidsApiClient,
) : StoreRepository {

    override suspend fun getPetsCatalog(studentId: String): List<PetCatalogDto> {
        return try {
            val response = api.httpClient.get(ApiConstants.storePetsCatalog(studentId))
            if (!response.status.isSuccess()) return emptyList()
            response.body<List<PetCatalogDto>>()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getAccessoriesCatalog(studentId: String): List<AccessoryCatalogDto> {
        return try {
            val response = api.httpClient.get(ApiConstants.storeAccessoriesCatalog(studentId))
            if (!response.status.isSuccess()) return emptyList()
            response.body<List<AccessoryCatalogDto>>()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun buyPet(studentId: String, request: BuyPetRequestDto): BuyPetResponseDto? {
        return try {
            val response = api.httpClient.post(ApiConstants.storeBuyPet(studentId)) {
                setBody(request)
            }
            if (!response.status.isSuccess()) return null
            response.body<BuyPetResponseDto>()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun buyAccessory(
        studentId: String,
        request: BuyAccessoryRequestDto,
    ): BuyAccessoryResponseDto? {
        return try {
            val response = api.httpClient.post(ApiConstants.storeBuyAccessory(studentId)) {
                setBody(request)
            }
            if (!response.status.isSuccess()) return null
            response.body<BuyAccessoryResponseDto>()
        } catch (_: Exception) {
            null
        }
    }
}
